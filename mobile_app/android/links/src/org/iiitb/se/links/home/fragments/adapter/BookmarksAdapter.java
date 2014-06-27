package org.iiitb.se.links.home.fragments.adapter;

import it.gmariotti.cardslib.library.view.CardView;

import java.util.List;

import org.iiitb.se.links.home.cards.BookmarkCard;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class BookmarksAdapter extends BaseAdapter {

  private Context context;
  private final List<JSONObject> bookmarks;
  
  public BookmarksAdapter(Context context, List<JSONObject> bookmarks) {
    super();
    this.context = context;
    this.bookmarks = bookmarks;
  }

  @Override
  public int getCount() {
    if (null != bookmarks) {
      return bookmarks.size();
    } else {
      return 0;
    }
  }

  @Override
  public Object getItem(int index) {
    JSONObject bookmark = null;
    if (null != bookmarks) {

      bookmark = bookmarks.get(index);
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

    bookmark = bookmarks.get(position);

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
