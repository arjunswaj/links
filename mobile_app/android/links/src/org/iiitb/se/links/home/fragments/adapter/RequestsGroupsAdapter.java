package org.iiitb.se.links.home.fragments.adapter;

import java.util.List;

import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.home.cards.AbstractGroupCard;
import org.iiitb.se.links.home.cards.RequestsGroupCard;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class RequestsGroupsAdapter extends AbstractGroupsAdapter {
  public RequestsGroupsAdapter(Context context, List<JSONObject> groups) {
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
      card = (RequestsGroupCard) cardView.getCard();
      card.setGroup(group);
    } else {
      card = new RequestsGroupCard(context, group);
      cardView = new CardView(context);
      cardView.setCard(card);
    }

    cardView.refreshCard(card);
    return cardView;
  }
}
