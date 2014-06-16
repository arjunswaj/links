package org.iiitb.se.links;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

  String apiKey = "8801f72043f447d0d0dc70bedee0c169d408591c9fab600ca02b4f93b667e8fc";
  String apiSecret = "fb6e2fe2b55f9d41fe57fbf7b515332ab42a44a7ebdc49f5952aa57fc6b86f70";

  private static final String CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob";
  private static final String PROTECTED_RESOURCE_URL = "http://links.t.proxylocal.com/api/bookmarks";

  private WebView mWebView;
  private OAuthService mOauthService;
  private Token mRequestToken;
  private static final Token EMPTY_TOKEN = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(apiKey).apiSecret(apiSecret).callback(CALLBACK_URL).build();
    setContentView(R.layout.activity_main);
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
      if ((url != null) && (url.startsWith(CALLBACK_URL))) {
        // Override webview when user came back to CALLBACK_URL
        mWebView.stopLoading();
        mWebView.setVisibility(View.INVISIBLE); // Hide webview if necessary
        Uri uri = Uri.parse(url);
        final Verifier verifier = new Verifier(
            uri.getQueryParameter("oauth_verifier"));
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
                    PROTECTED_RESOURCE_URL);
                mOauthService.signRequest(accessToken, request);
                Response response = request.send();
                return response.getBody();
              }

              @Override
              protected void onPostExecute(String result) {

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
