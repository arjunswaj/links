package org.iiitb.se.links.home.cards;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.expand.BookmarkCardExpand;
import org.iiitb.se.links.utils.DomainExtractor;
import org.iiitb.se.links.utils.StringConstants;
import org.json.JSONException;
import org.json.JSONObject;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class BookmarkCard extends Card {

  protected TextView mDomain;
  protected TextView mUpdatedTime;
  protected TextView mMore;
  JSONObject bookmark;
  Context context;
  String id = null;
  String url = null;
  String title = null;
  String description = null;
  String formattedDate = null;
  CardView cardView = null;
  CardHeader header = null;
  BookmarkCardExpand bookmarkCardExpand = null;

  public JSONObject getBookmark() {
    return bookmark;
  }

  public void setBookmark(JSONObject bookmark) {
    this.bookmark = bookmark;
    initData();
    setData();
  }

  /**
   * Constructor with a custom inner layout
   * 
   * @param context
   */
  public BookmarkCard(Context context, JSONObject bookmark) {
    super(context, R.layout.bookmark_card);
    this.bookmark = bookmark;
    this.context = context;
    init();
  }

  /**
   * Init
   */
  private void init() {

    header = new CardHeader(context);
    // Add a popup menu. This method set OverFlow button to visible
    header.setButtonOverflowVisible(true);
    header
        .setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
          @Override
          public void onMenuItemClick(BaseCard card, MenuItem item) {            
            initData();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            context.startActivity(Intent.createChooser(intent,
                context.getString(R.string.share)));
          }
        });
    // Add a PopupMenuPrepareListener to add dynamically a menu entry it is
    // optional.
    header
        .setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
          @Override
          public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
            popupMenu.getMenu().add(context.getString(R.string.share));
            return true;
          }
        });
    addCardHeader(header);

    bookmarkCardExpand = new BookmarkCardExpand(context, bookmark);
    addCardExpand(bookmarkCardExpand);

    this.setOnClickListener(new OnCardClickListener() {
      @Override
      public void onClick(Card card, View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
      }
    });

  }

  private void initData() {
    try {
      id = bookmark.getString(StringConstants.ID);
      url = bookmark.getString(StringConstants.URL);
      title = bookmark.getString(StringConstants.TITLE);
      description = bookmark.getString(StringConstants.DESCRIPTION);
      long epoch = bookmark.getLong(StringConstants.UPDATED_AT);

      Date date = new Date(epoch * 1000);
      java.text.DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
          Locale.US);
      formattedDate = format.format(date);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void setData() {
    // Set the header title
    header.setTitle(title);
    bookmarkCardExpand.setBookmark(bookmark);
    mDomain.setText(DomainExtractor.getBaseDomain(url));
    mUpdatedTime.setText(formattedDate);
    setExpanded(false);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {
    // Retrieve elements
    mDomain = (TextView) parent.findViewById(R.id.bookmark_domain);
    mUpdatedTime = (TextView) parent.findViewById(R.id.bookmark_updated_time);
    mMore = (TextView) parent.findViewById(R.id.bookmark_more);
    initData();
    setData();
    ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder()
        .setupView(mMore);
    setViewToClickToExpand(viewToClickToExpand);
  }
}
