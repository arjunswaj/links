package org.iiitb.se.links.home.fragments;

import java.io.IOException;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class AddBookmarkFragment extends Fragment implements ResourceLoader {
  private static final String TAG = "AddBookmarkFragment";
  private WebView mWebView;
  private Dialog authDialog;
  private OAuthService mOauthService;
  private Token mRequestToken;
  private static final Token EMPTY_TOKEN = null;
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPreferencesEditor;
  private WebViewClient mWebViewClient;

  private EditText url;
  private EditText title;
  private EditText description;
  private EditText tags;
  private Button cancel;
  private Button ok;

  protected ProgressDialog mProgressDialog;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    sharedPreferences = getActivity()
        .getPreferences(getActivity().MODE_PRIVATE);
    sharedPreferencesEditor = getActivity().getPreferences(
        getActivity().MODE_PRIVATE).edit();
    View rootView = inflater.inflate(R.layout.save_bookmark, container, false);

    url = (EditText) rootView.findViewById(R.id.bookmark_url);
    title = (EditText) rootView.findViewById(R.id.bookmark_title);
    description = (EditText) rootView.findViewById(R.id.bookmark_description);
    tags = (EditText) rootView.findViewById(R.id.bookmark_tags);
    cancel = (Button) rootView.findViewById(R.id.cancel);
    ok = (Button) rootView.findViewById(R.id.ok);

    mProgressDialog = new ProgressDialog(getActivity());

    mProgressDialog.setMessage(getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    authDialog = new Dialog(getActivity());
    authDialog.setContentView(R.layout.auth_dialog);

    int i = getArguments().getInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER);

    String linkOption = getResources().getStringArray(R.array.links_options)[i];
    getActivity().setTitle(linkOption);

    String bookmarkUrl = getArguments().getString(StringConstants.URL);
    if (null != bookmarkUrl) {
      url.setText(bookmarkUrl);
      fetchWebPageDetails();
    }
    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

    mWebView = (WebView) authDialog.findViewById(R.id.webView);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebViewClient = new AuthorizationClient(getActivity(), authDialog,
        mOauthService, this, mRequestToken);
    mWebView.setWebViewClient(mWebViewClient);

    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        closeThisFragmentAndLoadHome();
      }
    });

    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        String accessTokenKey = sharedPreferences.getString(
            AppConstants.ACCESS_TOKEN_KEY, null);
        String accessTokenSecret = sharedPreferences.getString(
            AppConstants.ACCESS_TOKEN_SECRET, null);
        Token accessToken = new Token(accessTokenKey, accessTokenSecret);
        saveBookmark(accessToken);
      }
    });
    return rootView;

  }

  private void closeThisFragmentAndLoadHome() {
    getActivity().getFragmentManager().beginTransaction().remove(this).commit();

    Fragment fragment = new LinkFragment();
    Bundle args = new Bundle();
    args.putInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER, 0);
    fragment.setArguments(args);
    getActivity().getFragmentManager().beginTransaction()
        .replace(R.id.content_frame, fragment).commit();
  }

  private void saveBookmark(final Token accessToken) {
    (new AsyncTask<Void, Integer, String>() {
      Response response;
      int status;

      @Override
      protected void onPreExecute() {
        mProgressDialog.show();
      }

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = URLConstants.SAVE_BOOKMARK;
        OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
        request.addBodyParameter(StringConstants.URL, url.getText().toString());
        request.addBodyParameter(StringConstants.TITLE, title.getText()
            .toString());
        request.addBodyParameter(StringConstants.DESCRIPTION, description
            .getText().toString());
        request.addBodyParameter(StringConstants.TAGS, tags.getText()
            .toString());

        mOauthService.signRequest(accessToken, request);
        response = request.send();
        status = response.getCode();
        return response.getBody();
      }

      @Override
      protected void onPostExecute(String responseBody) {
        mProgressDialog.hide();
        if (null == responseBody || 401 == status) {
          startAuthorize();
        } else {
          hideKeyboard();
          closeThisFragmentAndLoadHome();
        }
      }
    }).execute();
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getActivity()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(description.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(tags.getWindowToken(), 0);

  }

  public AddBookmarkFragment() {
    // Empty constructor required for fragment subclasses
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    saveBookmark(accessToken);
  }

  @Override
  public void startAuthorize() {
    (new AsyncTask<Void, Integer, String>() {
      @Override
      protected void onPreExecute() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
      }

      @Override
      protected void onProgressUpdate(Integer... progress) {
        mProgressDialog.setProgress(progress[0]);
      }

      @Override
      protected String doInBackground(Void... params) {
        return mOauthService.getAuthorizationUrl(EMPTY_TOKEN);
      }

      @Override
      protected void onPostExecute(String url) {
        mProgressDialog.hide();
        mWebView.loadUrl(url);
      }

    }).execute();
  }

  private void fetchWebPageDetails() {

    (new AsyncTask<Void, Integer, Document>() {
      @Override
      protected void onPreExecute() {
        mProgressDialog.show();
      }

      @Override
      protected Document doInBackground(Void... params) {
        Document doc = null;
        try {
          doc = Jsoup.connect(url.getText().toString()).get();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return doc;
      }

      @Override
      protected void onPostExecute(Document doc) {
        if (null != doc) {
          title.setText(doc.title());
          Elements metas = doc.select(StringConstants.META);
          for (Element meta : metas) {
            if (meta.attr(StringConstants.NAME).equals(
                StringConstants.DESCRIPTION)) {
              description.setText(meta.attr(StringConstants.CONTENT));
            }
            if (meta.attr(StringConstants.NAME)
                .equals(StringConstants.KEYWORDS)) {
              tags.setText(meta.attr(StringConstants.CONTENT));
            }
          }
        }
        mProgressDialog.hide();
        hideKeyboard();
      }

    }).execute();

  }
}
