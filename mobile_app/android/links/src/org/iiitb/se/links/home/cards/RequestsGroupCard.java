package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;

public class RequestsGroupCard extends AbstractGroupCard {

  private int actionStatus = 0;

  private MenuItem accept = null;
  private MenuItem reject = null;

  public RequestsGroupCard(Context context, JSONObject group) {
    super(context, group);
  }

  private void acceptOrRejectSubscriptionToGroup() {
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
          resourceURL = URLConstants.ACCEPT_SUBSCRIBE + "/" + id;
          request = new OAuthRequest(Verb.PUT, resourceURL);
        } else if (1 == actionStatus) {
          resourceURL = URLConstants.REJECT_SUBSCRIBE + "/" + id;
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
          reloadHome();
        }
      }
    }).execute();
  }

  /**
   * Init
   */
  protected void init() {
    header = new CardHeader(context);
    // Add a popup menu. This method set OverFlow button to visible
    header.setButtonOverflowVisible(true);
    header
        .setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
          @Override
          public void onMenuItemClick(BaseCard card, MenuItem item) {
            initData();
            if (item == accept) {
              actionStatus = 0;
            } else if (item == reject) {
              actionStatus = 1;
            }
            acceptOrRejectSubscriptionToGroup();
          }
        });
    // Add a PopupMenuPrepareListener to add dynamically a menu entry it is
    // optional.
    header
        .setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
          @Override
          public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
            accept = popupMenu.getMenu().add(
                context.getString(R.string.accept_group_invite));
            reject = popupMenu.getMenu().add(
                context.getString(R.string.reject_group_invite));
            return true;
          }
        });
    addCardHeader(header);
    otherInit();
  }
}
