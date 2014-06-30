package org.iiitb.se.links;

import org.iiitb.se.links.group.fragments.AddBookmarkInGroupFragment;
import org.iiitb.se.links.group.fragments.GroupLinkFragment;
import org.iiitb.se.links.home.fragments.BookmarkSearchFragment;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.FragmentTypes;
import org.iiitb.se.links.utils.StringConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class GroupActivity extends Activity {

  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;

  private CharSequence mDrawerTitle;
  private CharSequence mTitle;
  private String[] mLinksOptions;
  private SearchView searchView;
  public FragmentTypes fragmentTypes;
  private static final String TAG = "GroupActivity";

  public SearchView getSearchView() {
    return searchView;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group);
    Intent intent = this.getIntent();
    String action = intent.getAction();

    mTitle = mDrawerTitle = getTitle();
    mLinksOptions = getResources().getStringArray(R.array.groups_links_options);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout
        .setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, mLinksOptions));
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    // enable ActionBar app icon to behave as action to toggle nav drawer
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    searchView = new SearchView(getActionBar().getThemedContext());

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
    mDrawerLayout, /* DrawerLayout object */
    R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
    R.string.drawer_open, /* "open drawer" description for accessibility */
    R.string.drawer_close /* "close drawer" description for accessibility */
    ) {
      public void onDrawerClosed(View view) {
        getActionBar().setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        getActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
    mDrawerLayout.setDrawerListener(mDrawerToggle);
    if (null == savedInstanceState) {
      selectItem(0);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    searchView.setQueryHint(getResources().getString(R.string.search_hint));
    menu.add(Menu.NONE, Menu.NONE, 1, getResources().getString(R.string.search))
        .setIcon(R.drawable.action_search)
        .setActionView(searchView)
        .setShowAsAction(
            MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

    searchView.setOnQueryTextListener(new OnQueryTextListener() {
      @Override
      public boolean onQueryTextChange(String newText) {
        if (newText.length() > 0) {
          // Search

        } else {
          // Do something when there's no input
        }
        return false;
      }

      @Override
      public boolean onQueryTextSubmit(String query) {
        Fragment fragment = null;
        if (null != query) {
          InputMethodManager imm = (InputMethodManager) getApplication()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
          // Based on the fragment type loaded, go to the respective search
          // screen
          switch (fragmentTypes) {
            case GROUP_BOOKMARKS_FRAGMENT:
            case GROUP_BOOKMARKS_SEARCH_FRAGMENT:
              fragment = new BookmarkSearchFragment();
              fragmentTypes = FragmentTypes.GROUP_BOOKMARKS_SEARCH_FRAGMENT;
              break;
            case GROUP_ADD_BOOKMARK_FRAGMENT:

            default:
              break;

          }
          if (null != fragment) {
            Bundle args = new Bundle();
            args.putString(AppConstants.SEARCH_QUERY, query);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
          }
          return true;
        }
        return false;
      }
    });

    return true;
  }

  /* Called whenever we call invalidateOptionsMenu() */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    // menu.findItem(R.id.action_links_search).setVisible(!drawerOpen);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // The action bar home/up action should open or close the drawer.
    // ActionBarDrawerToggle will take care of this.
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle action buttons
    switch (item.getItemId()) {
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /* The click listner for ListView in the navigation drawer */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
      selectItem(position);
    }
  }

  private void selectItem(int position) {
    // update the main content by replacing fragments
    Fragment fragment = null;
    switch (position) {
      case 0:
        fragment = new GroupLinkFragment();
        fragmentTypes = FragmentTypes.GROUP_BOOKMARKS_FRAGMENT;
        break;
      case 1:
        fragmentTypes = FragmentTypes.GROUP_ADD_BOOKMARK_FRAGMENT;
        openDialogToAddURL();
        break;
    }
    if (null != fragment) {
      Bundle args = new Bundle();
      args.putInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER, position);
      fragment.setArguments(args);

      getFragmentManager().beginTransaction()
          .replace(R.id.content_frame, fragment).commit();
    }
    // update selected item and title, then close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(mLinksOptions[position]);
    mDrawerLayout.closeDrawer(mDrawerList);
  }

  private void openLinksSavePage(String urlFromIntent) {
    Fragment fragment = new AddBookmarkInGroupFragment();
    fragmentTypes = FragmentTypes.GROUP_ADD_BOOKMARK_FRAGMENT;
    Bundle args = new Bundle();
    args.putInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER, 1);
    args.putString(StringConstants.URL, urlFromIntent);

    fragment.setArguments(args);

    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
        .commit();

  }

  private void openDialogToAddURL() {
    LayoutInflater li = LayoutInflater.from(this);
    View promptsView = li.inflate(R.layout.bookmark_url, null);

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

    // set prompts.xml to alertdialog builder
    alertDialogBuilder.setView(promptsView);

    final EditText userInput = (EditText) promptsView
        .findViewById(R.id.bookmark_url);

    // set dialog message
    alertDialogBuilder
        .setCancelable(false)
        .setPositiveButton(getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                String url = userInput.getText().toString();
                if (null != url && !url.isEmpty()) {
                  url = url.trim();
                  openLinksSavePage(url);
                }
              }
            })
        .setNegativeButton(getString(android.R.string.cancel),
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
}
