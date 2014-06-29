package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.network.groups.subscribed.GroupUnsubscriber;
import org.json.JSONObject;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public class SubscribedGroupCard extends AbstractGroupCard {
  private MenuItem unsubscribe = null;
  private GroupUnsubscriber groupUnsubscriber;
  public SubscribedGroupCard(Context context, JSONObject group) {
    super(context, group);
    groupUnsubscriber = new GroupUnsubscriber(context, this);
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
              groupUnsubscriber.unsubscribeToGroup();
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
  }

  /**
   * Change the Intent
   */
  protected void loadGroupBookmarksFragment() {
    // TODO Auto-generated method stub

  }

}