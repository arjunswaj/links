package org.iiitb.se.links;

import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

  private WebView mWebView;
  private TextView tv;
  private Dialog auth_dialog;
  private OAuthService mOauthService;
  private Token mRequestToken;
  private static final Token EMPTY_TOKEN = null;
  private static final String TAG = "MainActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tv = (TextView) findViewById(R.id.result);
    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();    

    SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");      
      auth_dialog = new Dialog(MainActivity.this);
      auth_dialog.setContentView(R.layout.auth_dialog);
      mWebView = (WebView) auth_dialog.findViewById(R.id.webv);
      mWebView.clearCache(true);
      mWebView.getSettings().setJavaScriptEnabled(true);
      mWebView.setWebViewClient(mWebViewClient);
      auth_dialog.show();
      auth_dialog.setTitle("Authorize Links");
      auth_dialog.setCancelable(true);
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. Will access protected resource - bookmarks.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      fetchBookmarks(accessToken);
    }
  }

  private void fetchBookmarks(final Token accessToken) {
    (new AsyncTask<Void, Void, String>() {

      @Override
      protected String doInBackground(Void... params) {
        OAuthRequest request = new OAuthRequest(Verb.GET,
            URLConstants.PROTECTED_RESOURCE_URL_BOOKMARKS);
        mOauthService.signRequest(accessToken, request);
        Response response = request.send();

        return response.getBody();
      }

      @Override
      protected void onPostExecute(String result) {
        tv.setText(result);
      }

    }).execute();

  }

  private void startAuthorize() {
    (new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        return mOauthService.getAuthorizationUrl(EMPTY_TOKEN);
      }

      @Override
      protected void onPostExecute(String url) {
        mWebView.loadUrl(url);
      }
    }).execute();
  }

  private WebViewClient mWebViewClient = new WebViewClient() {
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      if ((url != null) && (url.startsWith(URLConstants.CALLBACK_URL))) {
        // Override webview when user came back to CALLBACK_URL
        mWebView.stopLoading();
        mWebView.setVisibility(View.INVISIBLE); // Hide webview if necessary
        String authorizationCode = url.substring(URLConstants.CALLBACK_URL
            .length());
        final Verifier verifier = new Verifier(authorizationCode);
        (new AsyncTask<Void, Void, Token>() {
          @Override
          protected Token doInBackground(Void... params) {
            Token token = mOauthService.getAccessToken(mRequestToken, verifier);
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE)
                .edit();
            editor.putString(AppConstants.ACCESS_TOKEN_KEY, token.getToken());
            editor.putString(AppConstants.ACCESS_TOKEN_SECRET,
                token.getSecret());
            editor.commit();
            Log.i(TAG, "Saving the token key.");
            return token;
          }

          @Override
          protected void onPostExecute(final Token accessToken) {
            auth_dialog.dismiss();
            fetchBookmarks(accessToken);
          }
        }).execute();
      } else {
        super.onPageStarted(view, url, favicon);
      }
    }
  };

}
