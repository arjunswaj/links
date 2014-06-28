package org.iiitb.se.links.home.fragments.adapter;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class AbstractGroupsAdapter extends BaseAdapter {

  protected Context context;
  protected final List<JSONObject> groups;

  public AbstractGroupsAdapter(Context context, List<JSONObject> groups) {
    super();
    this.context = context;
    this.groups = groups;
  }

  @Override
  public int getCount() {
    if (null != groups) {
      return groups.size();
    } else {
      return 0;
    }
  }

  @Override
  public Object getItem(int index) {
    JSONObject group = null;
    if (null != groups) {

      group = groups.get(index);
    }
    return groups;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }  

}
