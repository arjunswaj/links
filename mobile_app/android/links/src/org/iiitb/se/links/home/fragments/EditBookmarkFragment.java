package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.network.bookmarks.BookmarkEditor;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class EditBookmarkFragment extends Fragment {
  private static final String TAG = "EditBookmarkFragment";

  private EditText url;
  private EditText title;
  private EditText description;
  private EditText tags;
  private Button cancel;
  private Button ok;

  private BookmarkEditor bookmarkEditor;  

  private String bookmarkId;
  
  public String getBookmarkId() {
    return bookmarkId;
  }
  
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
    View rootView = inflater.inflate(R.layout.edit_bookmark, container, false);

    url = (EditText) rootView.findViewById(R.id.bookmark_url);
    title = (EditText) rootView.findViewById(R.id.bookmark_title);
    description = (EditText) rootView.findViewById(R.id.bookmark_description);
    tags = (EditText) rootView.findViewById(R.id.bookmark_tags);
    cancel = (Button) rootView.findViewById(R.id.cancel);
    ok = (Button) rootView.findViewById(R.id.ok);

    bookmarkEditor = new BookmarkEditor(getActivity(), this);    

    String linkOption = getString(R.string.edit_link);
    getActivity().setTitle(linkOption);

    bookmarkId = getArguments().getString(StringConstants.BOOKMARK_ID);
    String bookmarkUrl = getArguments().getString(StringConstants.URL);
    String bookmarkTitle = getArguments().getString(StringConstants.TITLE);
    String bookmarkDescription = getArguments().getString(StringConstants.DESCRIPTION);
    String bookmarkTags = getArguments().getString(StringConstants.TAGS);

    url.setText(bookmarkUrl);
    title.setText(bookmarkTitle);
    description.setText(bookmarkDescription);
    tags.setText(bookmarkTags);
    
    
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        closeThisFragmentAndLoadHome();
      }
    });

    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        bookmarkEditor.editBookmark();
      }
    });
    return rootView;

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

  public EditBookmarkFragment() {
    // Empty constructor required for fragment subclasses
  }
}
