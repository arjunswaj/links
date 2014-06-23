package org.iiitb.se.links.home.fragments.adapter;

import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.home.cards.BookmarkCard;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookmarksAdapter extends BaseAdapter {

	private Context context;
	private final JSONArray bookmarks;

	public BookmarksAdapter(Context context, JSONArray bookmarks) {
		super();
		this.context = context;
		this.bookmarks = bookmarks;
	}

	@Override
	public int getCount() {
		if (null != bookmarks) {
			return bookmarks.length();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int index) {
		JSONObject bookmark = null;
		if (null != bookmarks) {
			try {
				bookmark = bookmarks.getJSONObject(index);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return bookmark;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		JSONObject bookmark = null;
		try {
			bookmark = bookmarks.getJSONObject(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		CardView cardView = null;		
		BookmarkCard card = null;
		
		if (null != convertView) {
			cardView = (CardView) convertView;
			card = (BookmarkCard) cardView.getCard();
			card.setBookmark(bookmark);						
		} else {
			card = new BookmarkCard(context, bookmark);			
			cardView = new CardView(context);
			cardView.setCard(card);			
		}
		
		cardView.refreshCard(card);
		return cardView;
	}

}
