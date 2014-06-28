package org.iiitb.se.links.home.fragments;

import java.util.ArrayList;
import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.iiitb.se.links.home.fragments.adapter.AbstractGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

public abstract class AbstractGroupFragment extends Fragment implements
    ResourceLoader {
  protected int lastFirstVisible = 0;
  protected int lastVisibleItemCount = 0;
  protected int lastTotalItemCount = 0;
  protected WebView mWebView;
  protected Dialog authDialog;
  protected OAuthService mOauthService;
  protected Token mRequestToken;
  protected ListView mListView;
  protected AbstractGroupsAdapter groupsAdapter;
  protected List<JSONObject> groups = new ArrayList<JSONObject>();
  protected static final Token EMPTY_TOKEN = null;
  private static final String TAG = "AbstractGroupFragment";
  protected SharedPreferences sharedPreferences;
  protected SharedPreferences.Editor sharedPreferencesEditor;
  protected WebViewClient mWebViewClient;

  protected ProgressDialog mProgressDialog;

  protected abstract void fetchGroups(final Token accessToken);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    sharedPreferences = getActivity()
        .getPreferences(getActivity().MODE_PRIVATE);
    sharedPreferencesEditor = getActivity().getPreferences(
        getActivity().MODE_PRIVATE).edit();
    View rootView = inflater
        .inflate(R.layout.fragment_groups, container, false);
    authDialog = new Dialog(getActivity());
    authDialog.setContentView(R.layout.auth_dialog);

    int i = getArguments().getInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER);
    String linkOption = getResources().getStringArray(R.array.links_options)[i];
    getActivity().setTitle(linkOption);

    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

    mProgressDialog = new ProgressDialog(getActivity());

    mProgressDialog.setMessage(getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    mWebView = (WebView) authDialog.findViewById(R.id.webView);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebViewClient = new AuthorizationClient(getActivity(), authDialog,
        mOauthService, this, mRequestToken);
    mWebView.setWebViewClient(mWebViewClient);

    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");
      authDialog.show();
      authDialog.setTitle(getResources().getString(R.string.authorize_links));
      // authDialog.setCancelable(true);
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. Will access protected resource - groups.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      fetchGroups(accessToken);
    }
    return rootView;
  }

  @Override
  public void fetchProtectedResource(final Token accessToken) {
    fetchGroups(accessToken);
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

}
