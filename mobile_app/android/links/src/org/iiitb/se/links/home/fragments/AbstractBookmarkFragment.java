package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.BookmarksAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.iiitb.se.links.utils.BookmarkLoadType;
import org.json.JSONArray;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Dialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public abstract class AbstractBookmarkFragment extends Fragment implements ResourceLoader {
  protected int lastFirstVisible = 0;
  protected int lastVisibleItemCount = 0;
  protected int lastTotalItemCount = 0;

  public static final String LINK_OPTION_NUMBER = "link_option_number";
  protected WebView mWebView;
  protected Dialog authDialog;
  protected OAuthService mOauthService;
  protected Token mRequestToken;
  protected ListView mListView;
  protected BookmarksAdapter bookmarksAdapter;
  protected JSONArray bookmarks = new JSONArray();
  protected static final Token EMPTY_TOKEN = null;
  private static final String TAG = "AbstractBookmarkFragment";
  protected SharedPreferences sharedPreferences;
  protected SharedPreferences.Editor sharedPreferencesEditor;
  protected WebViewClient mWebViewClient;
  
  protected abstract void fetchBookmarks(final Token accessToken,
      final BookmarkLoadType bookmarkLoadType);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    sharedPreferences = getActivity()
        .getPreferences(getActivity().MODE_PRIVATE);
    sharedPreferencesEditor = getActivity().getPreferences(
        getActivity().MODE_PRIVATE).edit();
    View rootView = inflater.inflate(R.layout.fragment_links, container, false);
    authDialog = new Dialog(getActivity());
    authDialog.setContentView(R.layout.auth_dialog);
          

    int i = getArguments().getInt(LINK_OPTION_NUMBER);
    String linkOption = getResources().getStringArray(R.array.links_options)[i];
    getActivity().setTitle(linkOption);

    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

    mWebView = (WebView) authDialog.findViewById(R.id.webView);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);  
    mWebViewClient = new AuthorizationClient(getActivity(), authDialog, mOauthService, this, mRequestToken);
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
      Log.i(TAG, "Token Key found. Will access protected resource - bookmarks.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      fetchBookmarks(accessToken, BookmarkLoadType.TIMELINE);
    }
    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mListView = (ListView) getActivity().findViewById(R.id.card_listview);
    bookmarksAdapter = new BookmarksAdapter(getActivity(), bookmarks);
    mListView.setAdapter(bookmarksAdapter);

    mListView.setOnScrollListener(new OnScrollListener() {

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {

      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
        boolean loadMore = /* maybe add a padding */
        (firstVisibleItem + visibleItemCount >= totalItemCount)
            && (totalItemCount != 0)
            && !isLastRequestSame(firstVisibleItem, visibleItemCount,
                totalItemCount);

        if (loadMore) {
          lastFirstVisible = firstVisibleItem;
          lastVisibleItemCount = visibleItemCount;
          lastTotalItemCount = totalItemCount;
          String accessTokenKey = sharedPreferences.getString(
              AppConstants.ACCESS_TOKEN_KEY, null);
          String accessTokenSecret = sharedPreferences.getString(
              AppConstants.ACCESS_TOKEN_SECRET, null);
          Token accessToken = new Token(accessTokenKey, accessTokenSecret);
          fetchBookmarks(accessToken, BookmarkLoadType.MORE_BOOKMARKS);
          Log.i(TAG, "firstVisibleItem: " + firstVisibleItem
              + ", visibleItemCount" + visibleItemCount + ", totalItemCount"
              + totalItemCount);
        }
      }

      private boolean isLastRequestSame(int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
        if (lastFirstVisible == firstVisibleItem
            && lastVisibleItemCount == visibleItemCount
            && lastTotalItemCount == totalItemCount) {
          return true;
        }
        return false;
      }

    });
  }
  
  @Override
  public void fetchProtectedResource(final Token accessToken) {
    fetchBookmarks(accessToken, BookmarkLoadType.TIMELINE);
  }
  
  @Override
  public void startAuthorize() {
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

}
