package org.iiitb.se.links.home.cards.expand.adapter;

import java.util.List;

import org.iiitb.se.links.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagViewAdapter extends ArrayAdapter<String> {
  private Context mContext;
  private List<String> tags;

  // Gets the context so it can be used later
  public TagViewAdapter(Context mContext, int textViewResourceId,
      List<String> tags) {
    super(mContext, textViewResourceId, tags);
    this.mContext = mContext;
    this.tags = tags;
  }

  @Override
  public int getCount() {
    int count = 0;
    if (null != tags) {
      count = tags.size();
    }
    return count;
  }

  @Override
  public String getItem(int index) {
    String tag = null;
    if (null != tags) {
      tag = tags.get(index);
    }
    return tag;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup viewGroup) {
    TextView textView = null;
    LayoutInflater mInflater = (LayoutInflater) mContext
        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.bookmark_tag, null);
      textView = (TextView) convertView.findViewById(R.id.bookmark_tag);
    } else {
      textView = (TextView) convertView;
    }
    textView.setText((String) getItem(position));
    return textView;
  }

}
