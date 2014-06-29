package org.iiitb.se.links.utils.network;

import org.iiitb.se.links.MainActivity;
import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public abstract class AbstractResourceDownloader implements ResourceLoader {

  private static final String TAG = "AbstractDownloadManager";
  protected WebView mWebView;
  protected Dialog authDialog;
  protected OAuthService mOauthService;
  protected Token mRequestToken;
  protected SharedPreferences sharedPreferences;
  protected SharedPreferences.Editor sharedPreferencesEditor;
  protected WebViewClient mWebViewClient;
  protected static final Token EMPTY_TOKEN = null;
  protected ProgressDialog mProgressDialog;
  protected Context context;

  public AbstractResourceDownloader(Context context) {
    this.context = context;
    sharedPreferences = ((MainActivity) context)
        .getPreferences(MainActivity.MODE_PRIVATE);
    sharedPreferencesEditor = ((MainActivity) context).getPreferences(
        MainActivity.MODE_PRIVATE).edit();

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
    mWebViewClient = new AuthorizationClient((Activity) context, authDialog,
        mOauthService, this, mRequestToken);
    mWebView.setWebViewClient(mWebViewClient);
  }

  protected boolean netAvailable() {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null
        && activeNetwork.isConnectedOrConnecting();
    if (!isConnected) {
      Toast.makeText(context,
          context.getString(R.string.internet_not_available),
          Toast.LENGTH_LONG).show();
    }

    return isConnected;
  }

  @Override
  public void startAuthorize() {
    if (netAvailable()) {
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
}
