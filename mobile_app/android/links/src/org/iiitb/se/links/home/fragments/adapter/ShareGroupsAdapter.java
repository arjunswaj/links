package org.iiitb.se.links.home.fragments.adapter;

import it.gmariotti.cardslib.library.view.CardView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iiitb.se.links.home.cards.ShareGroupCard;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class ShareGroupsAdapter extends AbstractGroupsAdapter implements
    ShareGroupCard.GroupClickListener {
  protected static final String TAG = "ShareGroupsAdapter";
  private Set<String> groupIdsToShareWith = new HashSet<String>();
  private ShareGroupCard card = null;

  public ShareGroupsAdapter(Context context, List<JSONObject> groups) {
    super(context, groups);
  }

  public Set<String> getGroupIdsToShareWith() {
    return groupIdsToShareWith;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    JSONObject group = null;

    group = groups.get(position);

    CardView cardView = null;

    if (null != convertView) {
      cardView = (CardView) convertView;
      card = (ShareGroupCard) cardView.getCard();
      card.setGroup(group);
    } else {
      card = new ShareGroupCard(context, group, this);
      cardView = new CardView(context);
      cardView.setCard(card);
    }

    cardView.refreshCard(card);
    return cardView;
  }

  @Override
  public void notifyClickedGroup(ShareGroupCard card) {
    groupIdsToShareWith.add(card.getId());
  }

  @Override
  public void notifyUnClickedGroup(ShareGroupCard card) {
    groupIdsToShareWith.remove(card.getId());
  }
}
