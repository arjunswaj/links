package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.ShareGroupsAdapter;
import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ShareGroupCard extends Card {

  public interface GroupClickListener {
    public void notifyClickedGroup(ShareGroupCard card);

    public void notifyUnClickedGroup(ShareGroupCard card);
  }

  private JSONObject group;
  private Context context;
  protected String id = null;
  protected String name = null;
  protected String description = null;
  protected CardView cardView = null;
  protected CardHeader header = null;
  private TextView mDescription;
  private CheckBox mCheckbox;
  private ShareGroupsAdapter shareGroupsAdapter;

  public ShareGroupCard(Context context, JSONObject group,
      ShareGroupsAdapter shareGroupsAdapter) {
    super(context, R.layout.group_share_card);
    this.group = group;
    this.context = context;
    this.shareGroupsAdapter = shareGroupsAdapter;
    header = new CardHeader(context);
    addCardHeader(header);
    initData();
  }

  public String getId() {
    return id;
  }

  public void setGroup(JSONObject group) {
    this.group = group;
    initData();
    setData();
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
    mCheckbox.setChecked(false);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {
    // Retrieve elements
    mDescription = (TextView) parent.findViewById(R.id.group_description);
    mCheckbox = (CheckBox) parent.findViewById(R.id.group_check);
    mCheckbox
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            Log.i(TAG, "isChecked:" + isChecked + ", groupId: " + id);
            if (isChecked) {
              shareGroupsAdapter.notifyClickedGroup(ShareGroupCard.this);
            } else {
              shareGroupsAdapter.notifyUnClickedGroup(ShareGroupCard.this);
            }
          }
        });
    initData();
    setData();
  }
}
