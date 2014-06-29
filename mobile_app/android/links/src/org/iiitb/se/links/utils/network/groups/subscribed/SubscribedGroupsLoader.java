package org.iiitb.se.links.utils.network.groups.subscribed;

import java.util.List;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.SubscribedGroupsAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.iiitb.se.links.utils.network.AbstractResourceDownloader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SubscribedGroupsLoader extends AbstractResourceDownloader {

  protected static final String TAG = "SubscribedGroupsLoader";
  protected SubscribedGroupsAdapter groupsAdapter;
  protected List<JSONObject> groups;

  public SubscribedGroupsLoader(Context context,
      SubscribedGroupsAdapter groupsAdapter, List<JSONObject> groups) {
    super(context);
    this.groupsAdapter = groupsAdapter;
    this.groups = groups;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    fetchGroups(accessToken);
  }

  public void authorizeOrLoadGroups() {
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
      Log.i(TAG, "Token Key found. Will access protected resource - subscribed groups.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      fetchGroups(accessToken);
    }
  }

  public void fetchGroups(final Token accessToken) {
    if (netAvailable()) {
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
              // Log.i(TAG, responseBody);
              JSONArray resp = new JSONArray(responseBody);
              for (int index = 0; index < resp.length(); index += 1) {
                groups.add(resp.getJSONObject(index));
              }

              if (0 < groups.size()) {
                groupsAdapter.notifyDataSetChanged();
                Log.i(TAG, "Fetched the Subscribed Groups");
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }

      }).execute();
    }
  }
}
