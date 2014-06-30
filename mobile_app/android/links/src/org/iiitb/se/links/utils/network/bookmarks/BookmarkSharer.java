package org.iiitb.se.links.utils.network.bookmarks;

import java.util.Set;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.BookmarkCard;
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

public class BookmarkSharer extends AbstractResourceDownloader {

  private static final String TAG = "RequestsGroupsLoader";
  private BookmarkCard bookmarkCard;
  
  public BookmarkSharer(Context context, BookmarkCard bookmarkCard) {
    super(context);
    this.bookmarkCard = bookmarkCard;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    shareBookmark(accessToken);
  }

  public void shareBookmarkWithGroupsIamPartOf() {
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
      Log.i(TAG, "Token Key found. We're gonna share the bookmark with groups.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      shareBookmark(accessToken);
    }
  }

  private void shareBookmark(final Token accessToken) {
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
          String resourceURL = URLConstants.SHARE_BOOKMARK;
          OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
          request.addBodyParameter(StringConstants.BOOKMARK_ID, bookmarkCard.getBookmarkId());
          Set<String> groupIds = bookmarkCard.getGroupsAdapter().getGroupIdsToShareWith();
          for (String groupId : groupIds) {
            Log.i(TAG, "Group ID: " + groupId);
            request.addBodyParameter(StringConstants.GROUP_ID_ARRAY, groupId);
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
            reloadFragment();
          }
        }
      }).execute();
    }
  }

}
