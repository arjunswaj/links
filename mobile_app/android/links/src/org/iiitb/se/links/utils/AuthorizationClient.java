package org.iiitb.se.links.utils;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthorizationClient extends WebViewClient {
  private Activity activity;
  private Dialog authDialog;
  private OAuthService mOauthService;
  private ResourceLoader resourceLoader;
  private Token mRequestToken;
  private static final String TAG = "AuthorizationClient";
  private ProgressDialog mProgressDialog;
  
  public AuthorizationClient(Activity activity, Dialog authDialog,
      OAuthService mOauthService, ResourceLoader resourceLoader,
      Token mRequestToken) {
    super();
    this.activity = activity;
    this.authDialog = authDialog;
    this.mOauthService = mOauthService;
    this.resourceLoader = resourceLoader;
    this.mRequestToken = mRequestToken;
    
    mProgressDialog = new ProgressDialog(activity);
    mProgressDialog.setMessage(activity.getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    if (null != url) {
      if (url.equals(URLConstants.BASE_URL)
          || url.equals(URLConstants.BASE_URL + "/")) {
        authDialog.dismiss();
        resourceLoader.startAuthorize();
      } else if (url.startsWith(URLConstants.CALLBACK_URL)) {
        // Override webview when user came back to CALLBACK_URL
        view.stopLoading();
        view.setVisibility(View.INVISIBLE); // Hide webview if
        // necessary
        String authorizationCode = url.substring(URLConstants.CALLBACK_URL
            .length());
        final Verifier verifier = new Verifier(authorizationCode);
        (new AsyncTask<Void, Integer, Token>() {
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
          protected Token doInBackground(Void... params) {
            Token token = mOauthService.getAccessToken(mRequestToken, verifier);
            SharedPreferences.Editor editor = activity.getPreferences(
                activity.MODE_PRIVATE).edit();
            editor.putString(AppConstants.ACCESS_TOKEN_KEY, token.getToken());
            editor.putString(AppConstants.ACCESS_TOKEN_SECRET,
                token.getSecret());
            editor.commit();
            Log.i(TAG, "Saving the token key.");
            return token;
          }

          @Override
          protected void onPostExecute(final Token accessToken) {
            mProgressDialog.hide();
            authDialog.dismiss();
            resourceLoader.fetchProtectedResource(accessToken);
          }
        }).execute();
      }
    } else {
      super.onPageStarted(view, url, favicon);
    }
  }
}
