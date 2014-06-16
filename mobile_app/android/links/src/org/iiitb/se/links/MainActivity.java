package org.iiitb.se.links;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

  String apiKey = "8801f72043f447d0d0dc70bedee0c169d408591c9fab600ca02b4f93b667e8fc";
  String apiSecret = "fb6e2fe2b55f9d41fe57fbf7b515332ab42a44a7ebdc49f5952aa57fc6b86f70";

  WebView webView;
  Button auth;
  SharedPreferences pref;
  TextView access;

  private Token EMPTY_TOKEN = null;
  private static final String PROTECTED_RESOURCE_URL = "http://links.t.proxylocal.com/api/bookmarks";
  private static final String RESPONSE_TYPE_VALUE = "code";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    pref = getSharedPreferences("AppPref", MODE_PRIVATE);
    access = (TextView) findViewById(R.id.Access);
    auth = (Button) findViewById(R.id.auth);

    final OAuthService service = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(apiKey).apiSecret(apiSecret)
        .callback("urn:ietf:wg:oauth:2.0:oob").build();
    String authUrl = service.getAuthorizationUrl(EMPTY_TOKEN);

    webView = (WebView) findViewById(R.id.webv);
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        // This method will be executed each time a page finished loading.
        // The only we do is dismiss the progressDialog, in case we are
        // showing any.

      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view,
          String authorizationUrl) {
        // This method will be called when the Auth proccess redirect to our
        // RedirectUri.
        // We will check the url looking for our RedirectUri.

        Log.i("Authorize", "");
        Uri uri = Uri.parse(authorizationUrl);

        // If the user doesn't allow authorization to our application, the
        // authorizationToken Will be null.
        String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
        if (authorizationToken == null) {
          Log.i("Authorize", "The user doesn't allow authorization.");
          return true;
        }
        Log.i("Authorize", "Auth token received: " + authorizationToken);

        // Generate URL for requesting Access Token
        Verifier verifier = new Verifier(authorizationToken);
        Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        // We make the request in a AsyncTask
        OAuthRequest request = new OAuthRequest(Verb.GET,
            PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        Response response = request.send();

        return true;
      }
    });

    Log.i("Authorize", "Loading Auth Url: " + authUrl);
    // Load the authorization URL into the webView
    webView.loadUrl(authUrl);
    auth.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View arg0) {

      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
