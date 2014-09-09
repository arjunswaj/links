package org.iiitb.se.links.utils.network.bookmarks.search;

import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.BookmarksAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.BookmarkLoadType;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.iiitb.se.links.utils.network.AbstractResourceDownloader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkSearchLoader extends AbstractResourceDownloader {

  protected static final String TAG = "BookmarkSearchLoader";
  private BookmarksAdapter bookmarksAdapter;
  private List<JSONObject> bookmarks;
  private String searchQuery;

  public BookmarkSearchLoader(Context context,
      BookmarksAdapter bookmarksAdapter, List<JSONObject> bookmarks,
      String searchQuery) {
    super(context);
    this.bookmarksAdapter = bookmarksAdapter;
    this.bookmarks = bookmarks;
    this.searchQuery = searchQuery;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    fetchBookmarks(accessToken, BookmarkLoadType.TIMELINE);
  }

  public void authorizeOrLoadBookarks() {
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");
      authDialog.show();
      authDialog.setTitle(context.getString(R.string.authorize_links));
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. Will access protected resource - bookmarks.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      fetchBookmarks(accessToken, BookmarkLoadType.TIMELINE);
    }
  }

  public void fetchBookmarks(final BookmarkLoadType bookmarkLoadType) {
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    Token accessToken = new Token(accessTokenKey, accessTokenSecret);
    fetchBookmarks(accessToken, bookmarkLoadType);
  }

  protected void fetchBookmarks(final Token accessToken,
      final BookmarkLoadType bookmarkLoadType) {
    if (netAvailable()) {
      (new AsyncTask<Void, Integer, String>() {
        Response response;
        int status;

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
          String resourceURL = null;
          String lastBookmarkUpdatedAt = sharedPreferences.getString(
              AppConstants.LAST_SEARCH_BOOKMARK_UPDATED_AT, null);
          String BASE_URL = sharedPreferences.getString(AppConstants.BASE_URL, null);
          String query = Uri.encode(searchQuery);
          switch (bookmarkLoadType) {
            case MORE_BOOKMARKS:
              resourceURL = BASE_URL + URLConstants.SEARCH_MORE_BOOKMARKS + "/" + query
                  + "/" + lastBookmarkUpdatedAt;
              break;
            case REFRESH_BOOKMARKS:
              break;
            case TIMELINE:
              bookmarks.clear();
              resourceURL = BASE_URL + URLConstants.SEARCH + "/" + query;
              break;
            default:
              break;

          }

          OAuthRequest request = new OAuthRequest(Verb.GET, resourceURL);
          mOauthService.signRequest(accessToken, request);
          response = request.send();
          status = response.getCode();
          return response.getBody();
        }

        @Override
        protected void onPostExecute(String responseBody) {
          // Log.i(TAG, responseBody);
          mProgressDialog.hide();
          if (null == responseBody || 401 == status) {
            startAuthorize();
          } else {
            try {
              JSONArray resp = new JSONArray(responseBody);
              for (int index = 0; index < resp.length(); index += 1) {
                bookmarks.add(resp.getJSONObject(index));
              }

              if (0 < bookmarks.size()) {

                JSONObject linkObj = bookmarks.get(0);
                String updatedAt = linkObj
                    .getString(StringConstants.UPDATED_AT);
                sharedPreferencesEditor.putString(
                    AppConstants.FIRST_SEARCH_BOOKMARK_UPDATED_AT, updatedAt);

                linkObj = bookmarks.get(bookmarks.size() - 1);
                updatedAt = linkObj.getString(StringConstants.UPDATED_AT);
                sharedPreferencesEditor.putString(
                    AppConstants.LAST_SEARCH_BOOKMARK_UPDATED_AT, updatedAt);

                sharedPreferencesEditor.commit();
                bookmarksAdapter.notifyDataSetChanged();
                Log.i(TAG,
                    "Saving the updated time of first and last received bookmark");
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }

      }).execute();
    }
  }

}
