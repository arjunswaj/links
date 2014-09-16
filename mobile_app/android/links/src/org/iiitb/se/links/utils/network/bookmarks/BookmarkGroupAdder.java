package org.iiitb.se.links.utils.network.bookmarks;

import org.iiitb.se.links.R;
import org.iiitb.se.links.group.fragments.AddBookmarkInGroupFragment;
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

public class BookmarkGroupAdder extends AbstractResourceDownloader {

  private static final String TAG = "BookmarkGroupAdder";
  private AddBookmarkInGroupFragment addBookmarkInGroupFragment;
  private String groupId;

  public BookmarkGroupAdder(Context context,
      AddBookmarkInGroupFragment addBookmarkInGroupFragment, String groupId) {
    super(context);
    this.addBookmarkInGroupFragment = addBookmarkInGroupFragment;
    this.groupId = groupId;
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
      Log.i(TAG, "Token Key found. We're gonna save the bookmark in the group.");
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
          String resourceURL = URLConstants.SAVE_BOOKMARK_IN_GROUPS;          
          OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
          request.addBodyParameter(StringConstants.URL,
              addBookmarkInGroupFragment.getUrl().getText().toString());
          request.addBodyParameter(StringConstants.TITLE,
              addBookmarkInGroupFragment.getTitle().getText().toString());
          request.addBodyParameter(StringConstants.DESCRIPTION,
              addBookmarkInGroupFragment.getDescription().getText().toString());
          request.addBodyParameter(StringConstants.TAGS,
              addBookmarkInGroupFragment.getTags().getText().toString());

          request.addBodyParameter(StringConstants.GROUP_ID_ARRAY, groupId);
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
            addBookmarkInGroupFragment.hideKeyboard();
            addBookmarkInGroupFragment.closeThisFragmentAndLoadHome();
            Log.i(TAG, "Successfully saved the bookmark in the group.");
          }
        }
      }).execute();
    }
  }

}
