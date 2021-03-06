package org.iiitb.se.links.utils.network.bookmarks;

import java.util.Set;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.AddBookmarkFragment;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.iiitb.se.links.utils.network.AbstractResourceDownloader;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkAdder extends AbstractResourceDownloader {

  private static final String TAG = "BookmarkAdder";
  private AddBookmarkFragment addBookmarkFragment;
  private Set<String> groupIds;

  public BookmarkAdder(Context context,
      AddBookmarkFragment addBookmarkFragment, Set<String> groupIds) {
    super(context);
    this.addBookmarkFragment = addBookmarkFragment;
    this.groupIds = groupIds;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    saveBookmark(accessToken);
  }

  public void saveBookmark() {
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
      Log.i(TAG, "Token Key found. We're gonna save the bookmark.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      saveBookmark(accessToken);
    }
  }

  private void saveBookmark(final Token accessToken) {
    if (netAvailable()) {
      (new AsyncTask<Void, Integer, String>() {
        Response response;
        int status;

        @Override
        protected void onPreExecute() {
          mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
          String resourceURL = URLConstants.SAVE_BOOKMARK;
          if (!groupIds.isEmpty()) {
            resourceURL = URLConstants.SAVE_BOOKMARK_IN_GROUPS;
          }

          OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
          request.addBodyParameter(StringConstants.URL, addBookmarkFragment
              .getUrl().getText().toString());
          request.addBodyParameter(StringConstants.TITLE, addBookmarkFragment
              .getTitle().getText().toString());
          request.addBodyParameter(StringConstants.DESCRIPTION,
              addBookmarkFragment.getDescription().getText().toString());
          request.addBodyParameter(StringConstants.TAGS, addBookmarkFragment
              .getTags().getText().toString());

          if (!groupIds.isEmpty()) {
            for (String groupId : groupIds) {
              request.addBodyParameter(StringConstants.GROUP_ID_ARRAY, groupId);
            }
          }
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
            addBookmarkFragment.hideKeyboard();
            addBookmarkFragment.closeThisFragmentAndLoadHome();
            Log.i(TAG, "Successfully saved the bookmark.");
          }
        }
      }).execute();
    }
  }

}
