package org.iiitb.se.links.group.fragments;

import java.util.ArrayList;
import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.BookmarksAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.BookmarkLoadType;
import org.iiitb.se.links.utils.StringConstants;
import org.iiitb.se.links.utils.network.MyProperties;
import org.iiitb.se.links.utils.network.groups.GroupTimelineLoader;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class GroupLinkFragment extends Fragment {
  private static final String TAG = "LinkFragment";

  private GroupTimelineLoader groupTimelineLoader;
  private int lastFirstVisible = 0;
  private int lastVisibleItemCount = 0;
  private int lastTotalItemCount = 0;
  private String groupId;
  private ListView mListView;
  private BookmarksAdapter bookmarksAdapter;
  private List<JSONObject> bookmarks = new ArrayList<JSONObject>();

  public GroupLinkFragment() {
    // Empty constructor required for fragment subclasses
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_links, container, false);
    int i = getArguments().getInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER);
    groupId = MyProperties.getInstance().groupId;

    String linkOption = getResources().getStringArray(R.array.groups_links_options)[i];
    getActivity().setTitle(linkOption);

    mListView = (ListView) rootView.findViewById(R.id.card_listview);
    bookmarksAdapter = new BookmarksAdapter(getActivity(), bookmarks);
    mListView.setAdapter(bookmarksAdapter);

    mListView.setOnScrollListener(new OnScrollListener() {

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {

      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
        boolean loadMore = /* maybe add a padding */
        (firstVisibleItem + visibleItemCount >= totalItemCount)
            && (totalItemCount != 0)
            && !isLastRequestSame(firstVisibleItem, visibleItemCount,
                totalItemCount);

        if (loadMore) {
          lastFirstVisible = firstVisibleItem;
          lastVisibleItemCount = visibleItemCount;
          lastTotalItemCount = totalItemCount;
          groupTimelineLoader.fetchBookmarks(BookmarkLoadType.MORE_BOOKMARKS);

          Log.i(TAG, "firstVisibleItem: " + firstVisibleItem
              + ", visibleItemCount" + visibleItemCount + ", totalItemCount"
              + totalItemCount);
        }
      }

      private boolean isLastRequestSame(int firstVisibleItem,
          int visibleItemCount, int totalItemCount) {
        if (lastFirstVisible == firstVisibleItem
            && lastVisibleItemCount == visibleItemCount
            && lastTotalItemCount == totalItemCount) {
          return true;
        }
        return false;
      }

    });
    groupTimelineLoader = new GroupTimelineLoader(getActivity(),
        bookmarksAdapter, bookmarks, groupId);
    groupTimelineLoader.authorizeOrLoadBookarks();
    return rootView;
  }

}
