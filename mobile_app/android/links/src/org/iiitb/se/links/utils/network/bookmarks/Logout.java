package org.iiitb.se.links.utils.network.bookmarks;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
import org.iiitb.se.links.utils.network.AbstractResourceDownloader;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class Logout extends AbstractResourceDownloader {

  protected static final String TAG = "Logout";

  public Logout(Context context) {
    super(context);
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    performLogout(accessToken);
  }

  public void performLogout() {
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
      Log.i(TAG, "Token Key found. Will logout.");
      Token accessToken = new Token(accessTokenKey, accessTokenSecret);
      performLogout(accessToken);
    }
  }

  public void performLogout(final Token accessToken) {
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
          String resourceURL = URLConstants.LOGOUT;
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
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();            
            mWebView.clearCache(true);
            sharedPreferencesEditor.clear();
            sharedPreferencesEditor.commit();            
            reloadFragment();
          }
        }

      }).execute();
    }
  }  
}
