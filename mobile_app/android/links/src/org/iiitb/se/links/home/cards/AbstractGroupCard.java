package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.MainActivity;
import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.iiitb.se.links.home.fragments.LinkFragment;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.iiitb.se.links.utils.FragmentTypes;
import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public abstract class AbstractGroupCard extends Card implements ResourceLoader {

  protected TextView mDescription;
  protected JSONObject group;
  protected Context context;
  protected String id = null;
  protected String name = null;
  protected String description = null;
  protected CardView cardView = null;
  protected CardHeader header = null;
  

  protected WebView mWebView;
  protected Dialog authDialog;
  protected OAuthService mOauthService;
  protected Token mRequestToken;
  protected static final Token EMPTY_TOKEN = null;
  protected static final String TAG = "GroupCard";
  protected SharedPreferences sharedPreferences;
  protected SharedPreferences.Editor sharedPreferencesEditor;
  protected WebViewClient mWebViewClient;

  protected ProgressDialog mProgressDialog;

  public JSONObject getGroup() {
    return group;
  }

  public void setGroup(JSONObject group) {
    this.group = group;
    initData();
    setData();
  }

  /**
   * Constructor with a custom inner layout
   * 
   * @param context
   */
  public AbstractGroupCard(Context context, JSONObject group) {
    super(context, R.layout.group_card);
    this.group = group;
    this.context = context;
    init();
  }
  
  protected abstract void init();

  protected void otherInit() {
    sharedPreferences = ((MainActivity) context)
        .getPreferences(context.MODE_PRIVATE);
    sharedPreferencesEditor = ((MainActivity) context).getPreferences(
        context.MODE_PRIVATE).edit();
    authDialog = new Dialog(context);
    authDialog.setContentView(R.layout.auth_dialog);
    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

    mProgressDialog = new ProgressDialog(context);

    mProgressDialog.setMessage(context.getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    mWebView = (WebView) authDialog.findViewById(R.id.webView);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebViewClient = new AuthorizationClient((MainActivity) context,
        authDialog, mOauthService, this, mRequestToken);
    mWebView.setWebViewClient(mWebViewClient);
  }
  
  protected void reloadHome() {
    ((MainActivity) context).fragmentTypes = FragmentTypes.BOOKMARK_FRAGMENT;
    Fragment fragment = new LinkFragment();
    Bundle args = new Bundle();
    args.putInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER,
        FragmentTypes.BOOKMARK_FRAGMENT.ordinal());
    fragment.setArguments(args);
    ((MainActivity) context).getFragmentManager().beginTransaction()
        .replace(R.id.content_frame, fragment).commit();
  }

 

  protected void initData() {
    try {
      id = group.getString(StringConstants.ID);
      name = group.getString(StringConstants.GROUP_NAME);
      description = group.optString(StringConstants.DESCRIPTION,
          context.getString(R.string.group_description));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  protected void setData() {
    // Set the header title
    header.setTitle(name);
    mDescription.setText(description);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {
    // Retrieve elements
    mDescription = (TextView) parent.findViewById(R.id.group_description);
    initData();
    setData();
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    accessProtectedResource(accessToken);
  }

  protected abstract void accessProtectedResource(Token accessToken);

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
