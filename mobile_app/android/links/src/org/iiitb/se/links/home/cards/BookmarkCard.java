package org.iiitb.se.links.home.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.iiitb.se.links.MainActivity;
import org.iiitb.se.links.R;
import org.iiitb.se.links.home.cards.expand.BookmarkCardExpand;
import org.iiitb.se.links.home.fragments.LinkFragment;
import org.iiitb.se.links.home.fragments.adapter.ShareGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.DomainExtractor;
import org.iiitb.se.links.utils.FragmentTypes;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.network.bookmarks.BookmarkDeleter;
import org.iiitb.se.links.utils.network.bookmarks.BookmarkSharer;
import org.iiitb.se.links.utils.network.groups.subscribed.SubscribedGroupsLoader;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class BookmarkCard extends Card {

  protected TextView mDomain;
  protected TextView mUpdatedTime;
  protected TextView mMore;
  protected TextView mGroupInfo;
  private JSONObject bookmark;
  private Context context;

  private String id = null;
  private String url = null;
  private String title = null;
  private String description = null;
  private String groupname = null;
  private String username = null;
  private String formattedDate = null;

  private CardView cardView = null;
  private CardHeader header = null;
  private BookmarkCardExpand bookmarkCardExpand = null;

  private MenuItem shareWithApps = null;
  private MenuItem delete = null;
  private MenuItem shareWithGroups = null;
  private MenuItem edit = null;

  private static final String TAG = "BookmarkCard";

  private ListView mListView;
  private ShareGroupsAdapter groupsAdapter;
  private List<JSONObject> groups = new ArrayList<JSONObject>();

  private BookmarkDeleter bookmarkDeleter;
  private BookmarkSharer bookmarkSharer;
  private SubscribedGroupsLoader subscribedGroupsLoader;
  
  public JSONObject getBookmark() {
    return bookmark;
  }

  public void setBookmark(JSONObject bookmark) {
    this.bookmark = bookmark;
    initData();
    setData();
  }

  public String getBookmarkId() {
    return id;
  }

  public ShareGroupsAdapter getGroupsAdapter() {
    return groupsAdapter;
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
    bookmarkDeleter = new BookmarkDeleter(context, this);
    bookmarkSharer = new BookmarkSharer(context, this);    
    init();
  }
 

  protected void shareBookmarkWithGroups() {
    LayoutInflater li = LayoutInflater.from(context);
    View promptsView = li.inflate(R.layout.fragment_groups, null);

    mListView = (ListView) promptsView.findViewById(R.id.groups_card_listview);
    groupsAdapter = new ShareGroupsAdapter(context, groups);
    subscribedGroupsLoader = new SubscribedGroupsLoader(context, groupsAdapter, groups); 
    
    mListView.setAdapter(groupsAdapter);
    subscribedGroupsLoader.authorizeOrLoadGroups();

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

    // set prompts.xml to alertdialog builder
    alertDialogBuilder.setView(promptsView);

    // set dialog message
    alertDialogBuilder
        .setTitle(context.getString(R.string.share_with_groups))
        .setCancelable(false)
        .setPositiveButton(context.getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                bookmarkSharer.shareBookmarkWithGroupsIamPartOf();
              }
            })
        .setNegativeButton(context.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });

    // create alert dialog
    AlertDialog alertDialog = alertDialogBuilder.create();

    // show it
    alertDialog.show();

  }

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

  private void shareBookmarkWithApps() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, url);
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
    context.startActivity(Intent.createChooser(intent,
        context.getString(R.string.share_with_apps)));
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
            if (item == shareWithGroups) {
              shareBookmarkWithGroups();
            } else if (item == edit) {
              
            } else if (item == delete) {
              bookmarkDeleter.deleteBookmark();
            } else if (item == shareWithApps) {
              shareBookmarkWithApps();
            }
          }
        });
    // Add a PopupMenuPrepareListener to add dynamically a menu entry it is
    // optional.
    header
        .setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
          @Override
          public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
            shareWithGroups = popupMenu.getMenu().add(
                context.getString(R.string.share));
            edit = popupMenu.getMenu().add(context.getString(R.string.edit));
            delete = popupMenu.getMenu()
                .add(context.getString(R.string.delete));
            shareWithApps = popupMenu.getMenu().add(
                context.getString(R.string.share_with_apps));
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
      groupname = bookmark.optString(StringConstants.LINK_GROUP_NAME, null);
      username = bookmark.optString(StringConstants.USER_NAME, null);

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

    if (null != groupname) {
      mGroupInfo.setText(context.getString(R.string.bookmark_sharer, username,
          groupname));
    } else {
      mGroupInfo.setText("");
    }

    setExpanded(false);
  }

  @Override
  public void setupInnerViewElements(ViewGroup parent, View view) {
    // Retrieve elements
    mDomain = (TextView) parent.findViewById(R.id.bookmark_domain);
    mUpdatedTime = (TextView) parent.findViewById(R.id.bookmark_updated_time);
    mMore = (TextView) parent.findViewById(R.id.bookmark_more);
    mGroupInfo = (TextView) parent.findViewById(R.id.bookmark_shared_group);

    initData();
    setData();
    ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder()
        .setupView(mMore);
    setViewToClickToExpand(viewToClickToExpand);
  }
}
