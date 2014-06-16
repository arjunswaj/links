package org.iiitb.se.links;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, new PlaceholderFragment()).commit();
    }
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

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    String apiKey = "8801f72043f447d0d0dc70bedee0c169d408591c9fab600ca02b4f93b667e8fc";
    String apiSecret = "fb6e2fe2b55f9d41fe57fbf7b515332ab42a44a7ebdc49f5952aa57fc6b86f70";

    WebView webview;
    private Token EMPTY_TOKEN = null;

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      View rootView = inflater
          .inflate(R.layout.fragment_main, container, false);
      webview = (WebView) rootView.findViewById(R.id.webView1);

      OAuthService service = new ServiceBuilder().provider(LinksApi.class)
          .apiKey(apiKey).apiSecret(apiSecret)
          .callback("urn:ietf:wg:oauth:2.0:oob").build();
      String authUrl = service.getAuthorizationUrl(EMPTY_TOKEN);

      webview.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          if (url.startsWith("urn:ietf:wg:oauth:2.0:oob")) {
            Uri uri = Uri.parse(url);
            String oauthVerifier = uri.getQueryParameter("oauth_verifier");
            Verifier verifier = new Verifier(oauthVerifier);
            return true;
          }
          return super.shouldOverrideUrlLoading(view, url);
        }
      });
      webview.loadUrl(authUrl);
      return rootView;
    }
  }

}
