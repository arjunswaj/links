package org.iiitb.se.links.home.fragments.adapter;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

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
		JSONObject bookmark;
		String id = null;
		String url = null;
		String title = null;
		String description = null;
		CardView cardView = null;
		CardHeader header = null;
		Card card = null;
		
		try {
			bookmark = bookmarks.getJSONObject(position);
			id = bookmark.getString(StringConstants.ID);
			url = bookmark.getString(StringConstants.URL);
			title = bookmark.getString(StringConstants.TITLE);
			description = bookmark.getString(StringConstants.DESCRIPTION);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (null != convertView) {
			cardView = (CardView) convertView;
			card = cardView.getCard();
			card.setTitle(description);
			header = card.getCardHeader();
			header.setTitle(title);
			cardView.refreshCard(card);
		} else {

			// Create a Card
			card = new Card(context);
			card.setTitle(description);

			// Create a CardHeader
			header = new CardHeader(context);

			// Set the header title
			header.setTitle(title);

			// Add a popup menu. This method set OverFlow button to visible
			header.setButtonOverflowVisible(true);
			header.setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
				@Override
				public void onMenuItemClick(BaseCard card, MenuItem item) {
					Toast.makeText(
							context,
							"Click on " + item.getTitle() + "-"
									+ ((Card) card).getCardHeader().getTitle(),
							Toast.LENGTH_SHORT).show();
				}
			});

			// Add a PopupMenuPrepareListener to add dynamically a menu
			// entry
			// it is optional.
			header.setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
				@Override
				public boolean onPreparePopupMenu(BaseCard card,
						PopupMenu popupMenu) {
					popupMenu.getMenu().add("Share");

					// You can remove an item with this code
					// popupMenu.getMenu().removeItem(R.id.action_settings);

					// return false; You can use return false to hidden the
					// button
					// and the popup

					return true;
				}
			});
			card.addCardHeader(header);

			// Set card in the cardView
			cardView = new CardView(context);
			cardView.setCard(card);
			

		}
		return cardView;
	}

}
