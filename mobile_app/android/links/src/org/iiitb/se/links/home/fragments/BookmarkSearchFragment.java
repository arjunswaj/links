package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.BookmarkLoadType;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Fragment that appears in the "content_frame", shows Links after Search
 */
public class BookmarkSearchFragment extends AbstractBookmarkFragment {
  private static final String TAG = "BookmarkSearchFragment";

  public BookmarkSearchFragment() {
    // Empty constructor required for fragment subclasses
  }

  protected void fetchBookmarks(final Token accessToken,
      final BookmarkLoadType bookmarkLoadType) {
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
        String query = Uri.encode(getArguments().getString(
            AppConstants.SEARCH_QUERY));
        switch (bookmarkLoadType) {
          case MORE_BOOKMARKS:
            resourceURL = URLConstants.SEARCH_MORE_BOOKMARKS + "/" + query
                + "/" + lastBookmarkUpdatedAt;
            break;
          case REFRESH_BOOKMARKS:
            break;
          case TIMELINE:
            bookmarks.clear();
            resourceURL = URLConstants.SEARCH + "/" + query;
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
              String updatedAt = linkObj.getString(StringConstants.UPDATED_AT);
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
