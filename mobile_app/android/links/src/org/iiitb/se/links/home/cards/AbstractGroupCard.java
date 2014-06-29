package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.MainActivity;
import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.LinkFragment;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.FragmentTypes;
import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class AbstractGroupCard extends Card {

  protected TextView mDescription;
  protected JSONObject group;
  protected Context context;
  protected String id = null;
  protected String name = null;
  protected String description = null;
  protected CardView cardView = null;
  protected CardHeader header = null;

  protected static final String TAG = "GroupCard";

  public String getGroupId() {
    return id;
  }
  
  public JSONObject getGroup() {
    return group;
  }

  public void setGroup(JSONObject group) {
    this.group = group;
    initData();
    setData();
  }

  /**
   * Constructor with a custom inner layout
   * 
   * @param context
   */
  public AbstractGroupCard(Context context, JSONObject group) {
    super(context, R.layout.group_card);
    this.group = group;
    this.context = context;
    init();
  }

  public AbstractGroupCard(Context context, JSONObject group, int layout) {
    super(context, layout);
    this.group = group;
    this.context = context;
    init();
  }

  protected abstract void init();

  public void reloadHome() {
    ((MainActivity) context).fragmentTypes = FragmentTypes.BOOKMARK_FRAGMENT;
    Fragment fragment = new LinkFragment();
    Bundle args = new Bundle();
    args.putInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER,
        FragmentTypes.BOOKMARK_FRAGMENT.ordinal());
    fragment.setArguments(args);
    ((MainActivity) context).getFragmentManager().beginTransaction()
        .replace(R.id.content_frame, fragment).commit();
  }

  protected void initData() {
    try {
      id = group.getString(StringConstants.ID);
      name = group.getString(StringConstants.GROUP_NAME);
      description = group.optString(StringConstants.DESCRIPTION,
          context.getString(R.string.group_description));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  protected void setData() {
    // Set the header title
    header.setTitle(name);
    mDescription.setText(description);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {
    // Retrieve elements
    mDescription = (TextView) parent.findViewById(R.id.group_description);
    initData();
    setData();
  }
}
