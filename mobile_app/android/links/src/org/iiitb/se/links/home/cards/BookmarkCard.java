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
import java.util.Set;

import org.iiitb.se.links.MainActivity;
import org.iiitb.se.links.R;
import org.iiitb.se.links.home.ResourceLoader;
import org.iiitb.se.links.home.cards.expand.BookmarkCardExpand;
import org.iiitb.se.links.home.fragments.LinkFragment;
import org.iiitb.se.links.home.fragments.adapter.ShareGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.AuthorizationClient;
import org.iiitb.se.links.utils.BookmarkOperations;
import org.iiitb.se.links.utils.DomainExtractor;
import org.iiitb.se.links.utils.FragmentTypes;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinksApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class BookmarkCard extends Card implements ResourceLoader {

  protected TextView mDomain;
  protected TextView mUpdatedTime;
  protected TextView mMore;
  protected TextView mGroupInfo;
  JSONObject bookmark;
  Context context;
  String id = null;
  String url = null;
  String title = null;
  String description = null;
  String groupname = null;
  String username = null;
  String formattedDate = null;
  CardView cardView = null;
  CardHeader header = null;
  BookmarkCardExpand bookmarkCardExpand = null;
  MenuItem shareWithApps = null;
  MenuItem delete = null;
  MenuItem shareWithGroups = null;
  MenuItem edit = null;

  BookmarkOperations bookmarkOperations;

  private WebView mWebView;
  private Dialog authDialog;
  private OAuthService mOauthService;
  private Token mRequestToken;
  private static final Token EMPTY_TOKEN = null;
  private static final String TAG = "BookmarkCard";
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPreferencesEditor;
  protected List<JSONObject> groups = new ArrayList<JSONObject>();
  private WebViewClient mWebViewClient;

  protected ListView mListView;
  protected ShareGroupsAdapter groupsAdapter;
  protected ProgressDialog mProgressDialog;

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

  private void getShareableGroups() {
    bookmarkOperations = BookmarkOperations.GET_SHARABLE_GROUPS;
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");
      authDialog.show();
      authDialog.setTitle(context.getString(R.string.authorize_links));
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. We're gonna get sharable the bookmark.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      getShareableGroups(accessToken);
    }

  }

  private void getShareableGroups(final Token accessToken) {
    (new AsyncTask<Void, Integer, String>() {
      Response response;
      int status;

      @Override
      protected void onPreExecute() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
      }

      @Override
      protected void onProgressUpdate(Integer... progress) {
        mProgressDialog.setProgress(progress[0]);
      }

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = URLConstants.SUBSCRIBED_GROUPS_INDEX;
        OAuthRequest request = new OAuthRequest(Verb.GET, resourceURL);
        mOauthService.signRequest(accessToken, request);
        response = request.send();
        status = response.getCode();
        return response.getBody();
      }

      @Override
      protected void onPostExecute(String responseBody) {
        mProgressDialog.hide();
        if (null == responseBody || 401 == status) {
          startAuthorize();
        } else {
          try {
            // System.out.println(responseBody);
            JSONArray resp = new JSONArray(responseBody);
            for (int index = 0; index < resp.length(); index += 1) {
              groups.add(resp.getJSONObject(index));
            }

            if (0 < groups.size()) {
              groupsAdapter.notifyDataSetChanged();
              Log.i(TAG, "Fetched the Groups");
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }

    }).execute();

  }

  private void deleteBookmark() {
    bookmarkOperations = BookmarkOperations.DELETE;
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");
      authDialog.show();
      authDialog.setTitle(context.getString(R.string.authorize_links));
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. We're gonna delete the bookmark.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      deleteBookmark(accessToken);
    }

  }

  private void deleteBookmark(final Token accessToken) {
    (new AsyncTask<Void, Integer, String>() {
      Response response;
      int status;

      @Override
      protected void onPreExecute() {
        mProgressDialog.show();
      }

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = URLConstants.DELETE_BOOKMARK + "/" + id;
        OAuthRequest request = new OAuthRequest(Verb.DELETE, resourceURL);
        mOauthService.signRequest(accessToken, request);
        response = request.send();
        status = response.getCode();
        return response.getBody();
      }

      @Override
      protected void onPostExecute(String responseBody) {
        mProgressDialog.hide();
        if (null == responseBody || 401 == status) {
          startAuthorize();
        } else {
          reloadHome();
        }
      }
    }).execute();
  }

  private void shareBookmarkWithGroupsIamPartOf() {
    bookmarkOperations = BookmarkOperations.SHARE;
    String accessTokenKey = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_KEY, null);
    String accessTokenSecret = sharedPreferences.getString(
        AppConstants.ACCESS_TOKEN_SECRET, null);
    if (null == accessTokenKey || null == accessTokenSecret) {
      Log.i(TAG, "Token Key is not saved. Will start authorization.");
      authDialog.show();
      authDialog.setTitle(context.getString(R.string.authorize_links));
      startAuthorize();
    } else {
      Log.i(TAG, "Token Key found. We're gonna share the bookmark with groups.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      shareBookmark(accessToken);
    }
  }

  private void shareBookmark(final Token accessToken) {
    (new AsyncTask<Void, Integer, String>() {
      Response response;
      int status;

      @Override
      protected void onPreExecute() {
        mProgressDialog.show();
      }

      @Override
      protected String doInBackground(Void... params) {
        String resourceURL = URLConstants.SHARE_BOOKMARK;
        OAuthRequest request = new OAuthRequest(Verb.POST, resourceURL);
        request.addBodyParameter(StringConstants.BOOKMARK_ID, id);
        Set<String> groupIds = groupsAdapter.getGroupIdsToShareWith();
        for (String groupId : groupIds) {
          Log.i(TAG, "Group ID: " + groupId);
          request.addBodyParameter(StringConstants.GROUP_ID_ARRAY, groupId);
        }
        mOauthService.signRequest(accessToken, request);
        response = request.send();
        status = response.getCode();
        return response.getBody();
      }

      @Override
      protected void onPostExecute(String responseBody) {
        mProgressDialog.hide();
        if (null == responseBody || 401 == status) {
          startAuthorize();
        } else {
          reloadHome();
        }
      }
    }).execute();

  }

  protected void shareBookmarkWithGroups() {
    LayoutInflater li = LayoutInflater.from(context);
    View promptsView = li.inflate(R.layout.fragment_groups, null);

    mListView = (ListView) promptsView.findViewById(R.id.groups_card_listview);
    groupsAdapter = new ShareGroupsAdapter(context, groups);
    mListView.setAdapter(groupsAdapter);
    getShareableGroups();

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
                shareBookmarkWithGroupsIamPartOf();
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

  private void reloadHome() {
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
              editBookmark();
            } else if (item == delete) {
              deleteBookmark();
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

    otherInit();
  }

  protected void editBookmark() {
    // TODO Auto-generated method stub

  }

  private void otherInit() {
    sharedPreferences = ((MainActivity) context)
        .getPreferences(context.MODE_PRIVATE);
    sharedPreferencesEditor = ((MainActivity) context).getPreferences(
        context.MODE_PRIVATE).edit();
    authDialog = new Dialog(context);
    authDialog.setContentView(R.layout.auth_dialog);
    mOauthService = new ServiceBuilder().provider(LinksApi.class)
        .apiKey(AppConstants.API_KEY).apiSecret(AppConstants.API_SECRET)
        .callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

    mProgressDialog = new ProgressDialog(context);

    mProgressDialog.setMessage(context.getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    mWebView = (WebView) authDialog.findViewById(R.id.webView);
    mWebView.clearCache(true);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebViewClient = new AuthorizationClient((MainActivity) context,
        authDialog, mOauthService, this, mRequestToken);
    mWebView.setWebViewClient(mWebViewClient);
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
      mGroupInfo.setText(context.getString(R.string.bookmark_sharer, username, groupname));
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

  @Override
  public void fetchProtectedResource(Token accessToken) {
    switch (bookmarkOperations) {
      case DELETE:
        deleteBookmark(accessToken);
        break;
      case EDIT:
        break;
      case SHARE:
        shareBookmark(accessToken);
        break;
      case GET_SHARABLE_GROUPS:
        getShareableGroups(accessToken);
        break;
      default:
        break;
    }
  }

  @Override
  public void startAuthorize() {
    (new AsyncTask<Void, Integer, String>() {
      @Override
      protected void onPreExecute() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
      }

      @Override
      protected void onProgressUpdate(Integer... progress) {
        mProgressDialog.setProgress(progress[0]);
      }

      @Override
      protected String doInBackground(Void... params) {
        return mOauthService.getAuthorizationUrl(EMPTY_TOKEN);
      }

      @Override
      protected void onPostExecute(String url) {
        mProgressDialog.hide();
        mWebView.loadUrl(url);
      }

    }).execute();

  }
}
