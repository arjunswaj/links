package org.iiitb.se.links;

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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

  private WebView mWebView;
  private OAuthService mOauthService;
  private Token mRequestToken;
  private static final Token EMPTY_TOKEN = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(URLConstants.API_KEY).apiSecret(URLConstants.API_SECRET)
        .callback(URLConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();
    setContentView(R.layout.activity_main);
    
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    String name = preferences.getString("Name","");
    mWebView = (WebView) findViewById(R.id.webv);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.getSettings().setBuiltInZoomControls(true);
    mWebView.setWebViewClient(mWebViewClient);
    // mWebView.setWebChromeClient(mWebChromeClient);

    startAuthorize();
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
            return mOauthService.getAccessToken(mRequestToken, verifier);
          }

          @Override
          protected void onPostExecute(final Token accessToken) {
            (new AsyncTask<Void, Void, String>() {

              @Override
              protected String doInBackground(Void... params) {
                OAuthRequest request = new OAuthRequest(Verb.GET,
                    URLConstants.PROTECTED_RESOURCE_URL);
                mOauthService.signRequest(accessToken, request);
                Response response = request.send();

                return response.getBody();
              }

              @Override
              protected void onPostExecute(String result) {
                Log.i("Resource", result);
              }

            }).execute();
          }
        }).execute();
      } else {
        super.onPageStarted(view, url, favicon);
      }
    }
  };

}
