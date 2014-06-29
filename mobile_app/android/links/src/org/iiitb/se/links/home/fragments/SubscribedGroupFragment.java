package org.iiitb.se.links.home.fragments;

import java.util.ArrayList;
import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.SubscribedGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.network.groups.subscribed.SubscribedGroupsLoader;
import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SubscribedGroupFragment extends Fragment {

  private static final String TAG = "SubscribedGroupFragment";
  private SubscribedGroupsLoader subscribedGroupsLoader;
  private SubscribedGroupsAdapter groupsAdapter;
  private List<JSONObject> groups = new ArrayList<JSONObject>();
  private ListView mListView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View rootView = inflater
        .inflate(R.layout.fragment_groups, container, false);

    int i = getArguments().getInt(AppConstants.LINK_FRAGMENT_OPTION_NUMBER);
    String linkOption = getResources().getStringArray(R.array.links_options)[i];
    getActivity().setTitle(linkOption);
    mListView = (ListView) rootView.findViewById(R.id.groups_card_listview);
    groupsAdapter = new SubscribedGroupsAdapter(getActivity(), groups);
    mListView.setAdapter(groupsAdapter);

    subscribedGroupsLoader = new SubscribedGroupsLoader(getActivity(),
        groupsAdapter, groups);

    subscribedGroupsLoader.authorizeOrLoadGroups();
    return rootView;
  }

}
