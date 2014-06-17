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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;

  private CharSequence mDrawerTitle;
  private CharSequence mTitle;
  private String[] mLinksOptions;

  private static final Token EMPTY_TOKEN = null;
  private static final String TAG = "MainActivity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTitle = mDrawerTitle = getTitle();
    mLinksOptions = getResources().getStringArray(R.array.links_options);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout
        .setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, mLinksOptions));
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // enable ActionBar app icon to behave as action to toggle nav drawer
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
    mDrawerLayout, /* DrawerLayout object */
    R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
    R.string.drawer_open, /* "open drawer" description for accessibility */
    R.string.drawer_close /* "close drawer" description for accessibility */
    ) {
      public void onDrawerClosed(View view) {
        getActionBar().setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        getActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    if (savedInstanceState == null) {
      selectItem(0);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /* Called whenever we call invalidateOptionsMenu() */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    menu.findItem(R.id.action_links_search).setVisible(!drawerOpen);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle action buttons
    switch (item.getItemId()) {
    case R.id.action_links_search:
      // create intent to perform web search for this planet
      Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
      intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
      // catch event that there's no activity to handle intent
      if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
      } else {
        Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG)
            .show();
      }
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /* The click listner for ListView in the navigation drawer */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
      selectItem(position);
    }
  }

  private void selectItem(int position) {
    // update the main content by replacing fragments
    Fragment fragment = new LinkFragment();
    Bundle args = new Bundle();
    args.putInt(LinkFragment.LINK_OPTION_NUMBER, position);
    fragment.setArguments(args);

    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
        .commit();

    // update selected item and title, then close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(mLinksOptions[position]);
    mDrawerLayout.closeDrawer(mDrawerList);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getActionBar().setTitle(mTitle);
  }

  /**
   * When using the ActionBarDrawerToggle, you must call it during
   * onPostCreate() and onConfigurationChanged()...
   */

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggls
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  /**
   * Fragment that appears in the "content_frame", shows Links
   */
  public static class LinkFragment extends Fragment {
    public static final String LINK_OPTION_NUMBER = "link_option_number";
    private WebView mWebView;
    private TextView tv;
    private Dialog auth_dialog;
    private OAuthService mOauthService;
    private Token mRequestToken;

    public LinkFragment() {
      // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_links, container,
          false);
      auth_dialog = new Dialog(getActivity());
      auth_dialog.setContentView(R.layout.auth_dialog);      
      mWebView = (WebView) auth_dialog.findViewById(R.id.webView);
      mWebView.clearCache(true);
      mWebView.getSettings().setJavaScriptEnabled(true);
      mWebView.setWebViewClient(mWebViewClient);
      
      int i = getArguments().getInt(LINK_OPTION_NUMBER);
      String linkOption = getResources().getStringArray(R.array.links_options)[i];
      getActivity().setTitle(linkOption);

      tv = (TextView) rootView.findViewById(R.id.result);
      mOauthService = new ServiceBuilder().provider(LinksApi.class)
          .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
          .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

      SharedPreferences sharedPreferences = getActivity().getPreferences(
          MODE_PRIVATE);
      String accessTokenKey = sharedPreferences.getString(
          AppConstants.ACCESS_TOKEN_KEY, null);
      String accessTokenSecret = sharedPreferences.getString(
          AppConstants.ACCESS_TOKEN_SECRET, null);
      if (null == accessTokenKey || null == accessTokenSecret) {
        Log.i(TAG, "Token Key is not saved. Will start authorization.");              
        auth_dialog.show();
        auth_dialog.setTitle("Authorize Links");
        auth_dialog.setCancelable(true);
        startAuthorize();
      } else {
        Log.i(TAG,
            "Token Key found. Will access protected resource - bookmarks.");
        Token accessToken = new Token(accessTokenKey, accessTokenSecret);
        fetchBookmarks(accessToken);
      }
      return rootView;
    }

    private void fetchBookmarks(final Token accessToken) {      
      (new AsyncTask<Void, Void, String>() {
        Response response;
        int status;
        @Override
        protected String doInBackground(Void... params) {
          OAuthRequest request = new OAuthRequest(Verb.GET,
              URLConstants.PROTECTED_RESOURCE_URL_BOOKMARKS);
          mOauthService.signRequest(accessToken, request);
          response = request.send();
          status = response.getCode();
          return response.getBody();
        }

        @Override
        protected void onPostExecute(String responseBody) {
          if(null == responseBody || 401 == status) {
            startAuthorize();
          } else {
            tv.setText(responseBody);
          }
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
              Token token = mOauthService.getAccessToken(mRequestToken,
                  verifier);
              SharedPreferences.Editor editor = getActivity().getPreferences(
                  MODE_PRIVATE).edit();
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

}
