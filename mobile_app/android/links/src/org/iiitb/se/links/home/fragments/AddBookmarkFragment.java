package org.iiitb.se.links.home.fragments;

import java.util.ArrayList;
import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.ShareGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.network.WebpageLoader;
import org.iiitb.se.links.utils.network.bookmarks.BookmarkAdder;
import org.iiitb.se.links.utils.network.groups.subscribed.SubscribedGroupsLoader;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class AddBookmarkFragment extends Fragment implements
    WebpageLoader.AddBookmarkFormElements {
  private static final String TAG = "AddBookmarkFragment";

  private EditText url;
  private EditText title;
  private EditText description;
  private EditText tags;
  private Button cancel;
  private Button ok;
  private CheckBox shareInGroup;

  private ShareGroupsAdapter shareGroupsAdapter;

  private BookmarkAdder bookmarkAdder;
  private WebpageLoader webpageLoader;
  private SubscribedGroupsLoader subscribedGroupsLoader;

  private List<JSONObject> groups = new ArrayList<JSONObject>();

  public EditText getUrl() {
    return url;
  }

  public EditText getTitle() {
    return title;
  }

  public EditText getDescription() {
    return description;
  }

  public EditText getTags() {
    return tags;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.save_bookmark, container, false);

    url = (EditText) rootView.findViewById(R.id.bookmark_url);
    title = (EditText) rootView.findViewById(R.id.bookmark_title);
    description = (EditText) rootView.findViewById(R.id.bookmark_description);
    tags = (EditText) rootView.findViewById(R.id.bookmark_tags);
    cancel = (Button) rootView.findViewById(R.id.cancel);
    ok = (Button) rootView.findViewById(R.id.ok);
    shareInGroup = (CheckBox) rootView.findViewById(R.id.group_check);

    shareGroupsAdapter = new ShareGroupsAdapter(getActivity(), groups);

    bookmarkAdder = new BookmarkAdder(getActivity(), this,
        shareGroupsAdapter.getGroupIdsToShareWith());

    webpageLoader = new WebpageLoader(getActivity(), this);

    int i = getArguments().getInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER);

    String linkOption = getResources().getStringArray(R.array.links_options)[i];
    getActivity().setTitle(linkOption);

    String bookmarkUrl = getArguments().getString(StringConstants.URL);
    if (null != bookmarkUrl) {
      url.setText(bookmarkUrl);
      String bookmarkTitle = getArguments().getString(StringConstants.TITLE);
      if (null == bookmarkTitle) {
        webpageLoader.fetchWebPageDetails(false);
      } else {
        title.setText(bookmarkTitle);
        webpageLoader.fetchWebPageDetails(true);
      }
    }

    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        closeThisFragmentAndLoadHome();
      }
    });

    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        bookmarkAdder.saveBookmark();
      }
    });

    shareInGroup
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              groups.clear();
              shareBookmarkWithGroups();
            } else {
              shareGroupsAdapter.getGroupIdsToShareWith().clear();
            }
          }
        });
    return rootView;

  }

  protected void shareBookmarkWithGroups() {
    LayoutInflater li = LayoutInflater.from(getActivity());
    View promptsView = li.inflate(R.layout.fragment_groups, null);

    ListView mListView = (ListView) promptsView
        .findViewById(R.id.groups_card_listview);
    subscribedGroupsLoader = new SubscribedGroupsLoader(getActivity(),
        shareGroupsAdapter, groups);

    mListView.setAdapter(shareGroupsAdapter);
    subscribedGroupsLoader.authorizeOrLoadGroups();

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
        getActivity());

    // set prompts.xml to alertdialog builder
    alertDialogBuilder.setView(promptsView);

    // set dialog message
    alertDialogBuilder
        .setTitle(getActivity().getString(R.string.select_groups))
        .setCancelable(false)
        .setPositiveButton(
            AddBookmarkFragment.this.getString(android.R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            })
        .setNegativeButton(
            AddBookmarkFragment.this.getString(android.R.string.cancel),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                shareInGroup.setChecked(false);
                shareGroupsAdapter.getGroupIdsToShareWith().clear();
                dialog.cancel();
              }
            });

    // create alert dialog
    AlertDialog alertDialog = alertDialogBuilder.create();

    // show it
    alertDialog.show();

  }

  public void closeThisFragmentAndLoadHome() {
    Intent intent = new Intent(getActivity(), getActivity().getClass());
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        | Intent.FLAG_ACTIVITY_NEW_TASK);
    getActivity().startActivity(intent);
  }

  public void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getActivity()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(description.getWindowToken(), 0);
    imm.hideSoftInputFromWindow(tags.getWindowToken(), 0);

  }

  public AddBookmarkFragment() {
    // Empty constructor required for fragment subclasses
  }
}
