package org.iiitb.se.links.utils.network.bookmarks;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.EditBookmarkFragment;
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

public class BookmarkEditor extends AbstractResourceDownloader {

  private static final String TAG = "BookmarkEditor";
  private EditBookmarkFragment editBookmarkFragment;

  public BookmarkEditor(Context context, EditBookmarkFragment editBookmarkFragment) {
    super(context);
    this.editBookmarkFragment = editBookmarkFragment;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    editBookmark(accessToken);
  }

  public void editBookmark() {
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
      Log.i(TAG, "Token Key found. We're gonna edit the bookmark.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      editBookmark(accessToken);
    }
  }

  private void editBookmark(final Token accessToken) {
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
          String resourceURL = URLConstants.UPDATE_BOOKMARK;
          OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
          request.addBodyParameter(StringConstants.BOOKMARK_ID, editBookmarkFragment
              .getBookmarkId());
          request.addBodyParameter(StringConstants.URL, editBookmarkFragment
              .getUrl().getText().toString());
          request.addBodyParameter(StringConstants.TITLE, editBookmarkFragment
              .getTitle().getText().toString());
          request.addBodyParameter(StringConstants.DESCRIPTION,
              editBookmarkFragment.getDescription().getText().toString());
          request.addBodyParameter(StringConstants.TAGS, editBookmarkFragment
              .getTags().getText().toString());

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
            editBookmarkFragment.hideKeyboard();
            editBookmarkFragment.closeThisFragmentAndLoadHome();
            Log.i(TAG, "Successfully updated the bookmark.");
          }
        }
      }).execute();
    }
  }

}
