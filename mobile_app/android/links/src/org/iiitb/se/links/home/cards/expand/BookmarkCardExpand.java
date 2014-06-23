package org.iiitb.se.links.home.cards.expand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.CardExpand;

import org.iiitb.se.links.R;
import org.iiitb.se.links.custom.ExpandableHeightGridView;
import org.iiitb.se.links.home.cards.expand.adapter.TagViewAdapter;
import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkCardExpand extends CardExpand {
	JSONObject bookmark;
	Context context;
	TextView bookmarkDescription;
	ExpandableHeightGridView bookmarkTags;
	JSONArray tags;
	TagViewAdapter tagsAdapter;
	String description;
	List<String> tagList = new ArrayList<String>();

	public BookmarkCardExpand(Context context, JSONObject bookmark) {
		super(context, R.layout.bookmark_card_expand);
		this.bookmark = bookmark;
		this.context = context;
	}

	public JSONObject getBookmark() {
		return bookmark;
	}

	public void setBookmark(JSONObject bookmark) {
		this.bookmark = bookmark;
		initData();
		setData();
	}

	private void setData() {
		if (null != bookmarkDescription) {
			bookmarkDescription.setText(description);
		}
		if (null != bookmarkTags) {
			tagsAdapter.notifyDataSetChanged();
			bookmarkTags.setExpanded(true);
		}
	}

	private void initData() {
		try {
			tags = bookmark.getJSONArray(StringConstants.TAGS);
			description = bookmark.getString(StringConstants.DESCRIPTION);
			tagList.clear();
			for (int index = 0; index < tags.length(); index += 1) {
				tagList.add(tags.getString(index));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {

		if (view == null)
			return;

		// Retrieve TextView elements
		bookmarkDescription = (TextView) view
				.findViewById(R.id.bookmark_description);
		bookmarkTags = (ExpandableHeightGridView) view.findViewById(R.id.bookmark_tags);
		bookmarkTags.setExpanded(true);
		initData();

		tagsAdapter = new TagViewAdapter(context,
				R.layout.bookmark_tag, tagList);

		bookmarkTags.setAdapter(tagsAdapter);

		bookmarkTags
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						Toast.makeText(context, ((TextView) v).getText(),
								Toast.LENGTH_SHORT).show();
					}
				});

		setData();
	}
	
}