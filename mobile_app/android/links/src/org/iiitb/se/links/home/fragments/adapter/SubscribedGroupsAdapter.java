package org.iiitb.se.links.home.fragments.adapter;

import java.util.List;

import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.home.cards.AbstractGroupCard;
import org.iiitb.se.links.home.cards.SubscribedGroupCard;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class SubscribedGroupsAdapter extends AbstractGroupsAdapter {
  public SubscribedGroupsAdapter(Context context, List<JSONObject> groups) {
    super(context, groups);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    JSONObject group = null;

    group = groups.get(position);

    CardView cardView = null;
    AbstractGroupCard card = null;

    if (null != convertView) {
      cardView = (CardView) convertView;
      card = (SubscribedGroupCard) cardView.getCard();
      card.setGroup(group);
    } else {
      card = new SubscribedGroupCard(context, group);
      cardView = new CardView(context);
      cardView.setCard(card);
    }

    cardView.refreshCard(card);
    return cardView;
  }
}
