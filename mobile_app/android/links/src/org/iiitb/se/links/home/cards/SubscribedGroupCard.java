package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
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
import android.view.View;
import android.widget.PopupMenu;

public class SubscribedGroupCard extends AbstractGroupCard {
  private MenuItem unsubscribe = null;

  public SubscribedGroupCard(Context context, JSONObject group) {
    super(context, group);
  }

  private void unsubscribeToGroup() {
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
    (new AsyncTask<Void, Integer, String>() {
      Response response;
      int status;

      @Override
      protected void onPreExecute() {
        mProgressDialog.show();
      }

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = URLConstants.UNSUBSCRIBE_GROUP + "/" + id;
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
            if (item == unsubscribe) {
              unsubscribeToGroup();
            }
          }
        });
    // Add a PopupMenuPrepareListener to add dynamically a menu entry it is
    // optional.
    header
        .setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
          @Override
          public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
            unsubscribe = popupMenu.getMenu().add(
                context.getString(R.string.unsubscribe_group));
            return true;
          }
        });
    addCardHeader(header);

    this.setOnClickListener(new OnCardClickListener() {
      @Override
      public void onClick(Card card, View view) {
        loadGroupBookmarksFragment();
      }
    });

    otherInit();
  }

  /**
   * Change the Intent
   */
  protected void loadGroupBookmarksFragment() {
    // TODO Auto-generated method stub

  }

}