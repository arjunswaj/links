package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.RequestsGroupsAdapter;
import org.iiitb.se.links.utils.URLConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class RequestsGroupFragment extends AbstractGroupFragment {

  private static final String TAG = "RequestsGroupFragment";

  @Override
  protected void fetchGroups(final Token accessToken) {
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
        String resourceURL = URLConstants.REQUESTS_GROUPS_INDEX;
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

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mListView = (ListView) getActivity()
        .findViewById(R.id.groups_card_listview);
    groupsAdapter = new RequestsGroupsAdapter(getActivity(), groups);
    mListView.setAdapter(groupsAdapter);
  }

}
