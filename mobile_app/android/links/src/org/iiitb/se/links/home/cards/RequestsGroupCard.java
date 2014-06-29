package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.network.groups.requests.GroupRequestsDecider;
import org.json.JSONObject;

import android.content.Context;
import android.view.MenuItem;
import android.widget.PopupMenu;

public class RequestsGroupCard extends AbstractGroupCard {

  
  private GroupRequestsDecider groupRequestsDecider;
  private MenuItem accept = null;
  private MenuItem reject = null;

  public RequestsGroupCard(Context context, JSONObject group) {
    super(context, group);
    groupRequestsDecider = new GroupRequestsDecider(context, this);
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
              groupRequestsDecider.setActionStatus(0);
            } else if (item == reject) {
              groupRequestsDecider.setActionStatus(1);
            }
            groupRequestsDecider.acceptOrRejectSubscriptionToGroup();
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
  }
}
