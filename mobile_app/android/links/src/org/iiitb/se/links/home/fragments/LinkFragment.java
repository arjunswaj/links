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

import android.os.AsyncTask;
import android.util.Log;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class LinkFragment extends AbstractBookmarkFragment {
  private static final String TAG = "LinkFragment";
  
  public LinkFragment() {
    // Empty constructor required for fragment subclasses
  }

  protected void fetchBookmarks(final Token accessToken,
      final BookmarkLoadType bookmarkLoadType) {
    (new AsyncTask<Void, Void, String>() {
      Response response;
      int status;

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = null;
        String lastBookmarkUpdatedAt = sharedPreferences.getString(
            AppConstants.LAST_BOOKMARK_UPDATED_AT, null);

        switch (bookmarkLoadType) {
          case MORE_BOOKMARKS:
            resourceURL = URLConstants.LOAD_MORE_BOOKMARKS + "/"
                + lastBookmarkUpdatedAt;
            break;
          case REFRESH_BOOKMARKS:
            break;
          case TIMELINE:
            resourceURL = URLConstants.TIMELINE;
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
        if (null == responseBody || 401 == status) {
          startAuthorize();
        } else {
          try {
            // System.out.println(responseBody);
            JSONArray resp = new JSONArray(responseBody);
            for (int index = 0; index < resp.length(); index += 1) {
              bookmarks.put(resp.get(index));
            }

            if (0 < bookmarks.length()) {

              JSONObject linkObj = bookmarks.getJSONObject(0);
              String updatedAt = linkObj.getString(StringConstants.UPDATED_AT);
              sharedPreferencesEditor.putString(
                  AppConstants.FIRST_BOOKMARK_UPDATED_AT, updatedAt);

              linkObj = bookmarks.getJSONObject(bookmarks.length() - 1);
              updatedAt = linkObj.getString(StringConstants.UPDATED_AT);
              sharedPreferencesEditor.putString(
                  AppConstants.LAST_BOOKMARK_UPDATED_AT, updatedAt);

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
