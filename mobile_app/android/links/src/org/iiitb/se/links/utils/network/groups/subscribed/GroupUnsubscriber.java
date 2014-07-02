package org.iiitb.se.links.utils.network.groups.subscribed;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.SubscribedGroupCard;
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

public class GroupUnsubscriber extends AbstractResourceDownloader {

  private static final String TAG = "GroupUnsubscriber";
  private SubscribedGroupCard subscribedGroupCard;

  public GroupUnsubscriber(Context context,
      SubscribedGroupCard subscribedGroupCard) {
    super(context);
    this.subscribedGroupCard = subscribedGroupCard;
  }

  public void unsubscribeToGroup() {
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
      Log.i(TAG, "Token Key found. We're gonna unsubscribe to the group.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      accessProtectedResource(accessToken);
    }

  }

  /**
   * Unsubscribe
   */
  protected void accessProtectedResource(final Token accessToken) {
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
          String resourceURL = URLConstants.UNSUBSCRIBE_GROUP + "/"
              + subscribedGroupCard.getGroupId();
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

  @Override
  public void fetchProtectedResource(Token accessToken) {
    accessProtectedResource(accessToken);
  }
}
