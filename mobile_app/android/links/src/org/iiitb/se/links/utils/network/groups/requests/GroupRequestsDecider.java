package org.iiitb.se.links.utils.network.groups.requests;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.RequestsGroupCard;
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

public class GroupRequestsDecider extends AbstractResourceDownloader {

  private static final String TAG = "GroupRequestsDecider";
  private RequestsGroupCard requestsGroupCard;
  private int actionStatus = 0;

  public GroupRequestsDecider(Context context,
      RequestsGroupCard requestsGroupCard) {
    super(context);
    this.requestsGroupCard = requestsGroupCard;
  }

  public int getActionStatus() {
    return actionStatus;
  }

  public void setActionStatus(int actionStatus) {
    this.actionStatus = actionStatus;
  }

  public void acceptOrRejectSubscriptionToGroup() {
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
   * Accept/Reject
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
          String resourceURL = null;
          OAuthRequest request = null;
          if (0 == actionStatus) {
            resourceURL = URLConstants.ACCEPT_SUBSCRIBE + "/"
                + requestsGroupCard.getGroupId();
            request = new OAuthRequest(Verb.PUT, resourceURL);
          } else if (1 == actionStatus) {
            resourceURL = URLConstants.REJECT_SUBSCRIBE + "/"
                + requestsGroupCard.getGroupId();
            request = new OAuthRequest(Verb.DELETE, resourceURL);
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
            requestsGroupCard.reloadHome();
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
