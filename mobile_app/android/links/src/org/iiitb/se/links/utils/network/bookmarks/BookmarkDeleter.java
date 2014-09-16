package org.iiitb.se.links.utils.network.bookmarks;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.BookmarkCard;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.iiitb.se.links.utils.network.AbstractResourceDownloader;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkDeleter extends AbstractResourceDownloader {

  private static final String TAG = "RequestsGroupsLoader";
  private BookmarkCard bookmarkCard;

  public BookmarkDeleter(Context context, BookmarkCard bookmarkCard) {
    super(context);
    this.bookmarkCard = bookmarkCard;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    deleteBookmark(accessToken);
  }

  public void deleteBookmark() {
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
      Log.i(TAG, "Token Key found. We're gonna delete the bookmark.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      deleteBookmark(accessToken);
    }

  }

  private void deleteBookmark(final Token accessToken) {
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
          String resourceURL = URLConstants.DELETE_BOOKMARK + "/"
              + bookmarkCard.getBookmarkId();
          OAuthRequest request = new OAuthRequest(Verb.DELETE, resourceURL);
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
            reloadFragment();
          }
        }
      }).execute();
    }
  }

}
