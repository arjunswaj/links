package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.R;
import org.iiitb.se.links.home.fragments.adapter.BookmarksAdapter;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.BookmarkLoadType;
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
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.app.Dialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class LinkFragment extends Fragment {
	int lastFirstVisible = 0;
	int lastVisibleItemCount = 0;
	int lastTotalItemCount = 0;		

	public static final String LINK_OPTION_NUMBER = "link_option_number";
	private WebView mWebView;
	private Dialog auth_dialog;
	private OAuthService mOauthService;
	private Token mRequestToken;
	protected ListView mListView;
	BookmarksAdapter bookmarksAdapter;
	JSONArray bookmarks = new JSONArray();
	private static final Token EMPTY_TOKEN = null;
	private static final String TAG = "LinkFragment";
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor sharedPreferencesEditor;

	public LinkFragment() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sharedPreferences = getActivity().getPreferences(
				getActivity().MODE_PRIVATE);
		sharedPreferencesEditor = getActivity().getPreferences(
				getActivity().MODE_PRIVATE).edit();
		View rootView = inflater.inflate(R.layout.fragment_links, container,
				false);
		auth_dialog = new Dialog(getActivity());
		auth_dialog.setContentView(R.layout.auth_dialog);
		mWebView = (WebView) auth_dialog.findViewById(R.id.webView);
		mWebView.clearCache(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(mWebViewClient);

		int i = getArguments().getInt(LINK_OPTION_NUMBER);
		String linkOption = getResources()
				.getStringArray(R.array.links_options)[i];
		getActivity().setTitle(linkOption);

		mOauthService = new ServiceBuilder().provider(LinksApi.class)
				.apiKey(AppConstants.API_KEY)
				.apiSecret(AppConstants.API_SECRET)
				.callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

		String accessTokenKey = sharedPreferences.getString(
				AppConstants.ACCESS_TOKEN_KEY, null);
		String accessTokenSecret = sharedPreferences.getString(
				AppConstants.ACCESS_TOKEN_SECRET, null);
		if (null == accessTokenKey || null == accessTokenSecret) {
			Log.i(TAG, "Token Key is not saved. Will start authorization.");
			auth_dialog.show();
			auth_dialog.setTitle("Authorize Links");
			// auth_dialog.setCancelable(true);
			startAuthorize();
		} else {
			Log.i(TAG,
					"Token Key found. Will access protected resource - bookmarks.");
			Token accessToken = new Token(accessTokenKey, accessTokenSecret);
			fetchBookmarks(accessToken, BookmarkLoadType.TIMELINE);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView = (ListView) getActivity().findViewById(R.id.card_listview);
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
						&& !isLastRequestSame(firstVisibleItem,
								visibleItemCount, totalItemCount);

				if (loadMore) {					
					lastFirstVisible = firstVisibleItem;
					lastVisibleItemCount = visibleItemCount;
					lastTotalItemCount = totalItemCount;
					String accessTokenKey = sharedPreferences.getString(
							AppConstants.ACCESS_TOKEN_KEY, null);
					String accessTokenSecret = sharedPreferences.getString(
							AppConstants.ACCESS_TOKEN_SECRET, null);
					Token accessToken = new Token(accessTokenKey,
							accessTokenSecret);
					fetchBookmarks(accessToken, BookmarkLoadType.MORE_BOOKMARKS);
					Log.i("Scroll Scrolling: ", "firstVisibleItem: "
							+ firstVisibleItem + ", visibleItemCount"
							+ visibleItemCount + ", totalItemCount"
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
	}

	private void fetchBookmarks(final Token accessToken,
			final BookmarkLoadType bookmarkLoadType) {
		(new AsyncTask<Void, Void, String>() {
			Response response;
			int status;

			@Override
			protected String doInBackground(Void... params) {
				String resourceURL = null;
				String lastBookmarkUpdatedAt = sharedPreferences.getString(
						AppConstants.LAST_BOOKMARK_UPDATED_AT, null);

				switch (bookmarkLoadType) {
				case MORE_BOOKMARKS:
					resourceURL = URLConstants.LOAD_MORE_BOOKMARKS + "/"
							+ lastBookmarkUpdatedAt;
					break;
				case REFRESH_BOOKMARKS:
					break;
				case TIMELINE:
					resourceURL = URLConstants.TIMELINE;
					break;
				default:
					break;

				}
				OAuthRequest request = new OAuthRequest(Verb.GET, resourceURL);
				mOauthService.signRequest(accessToken, request);
				response = request.send();
				status = response.getCode();
				return response.getBody();
			}

			@Override
			protected void onPostExecute(String responseBody) {
				if (null == responseBody || 401 == status) {
					startAuthorize();
				} else {
					try {
						// System.out.println(responseBody);
						JSONArray resp = new JSONArray(responseBody);
						for (int index = 0; index < resp.length(); index += 1) {
							bookmarks.put(resp.get(index));
						}

						if (0 < bookmarks.length()) {

							JSONObject linkObj = bookmarks.getJSONObject(0);
							String updatedAt = linkObj
									.getString(StringConstants.UPDATED_AT);
							sharedPreferencesEditor.putString(
									AppConstants.FIRST_BOOKMARK_UPDATED_AT,
									updatedAt);

							linkObj = bookmarks.getJSONObject(bookmarks
									.length() - 1);
							updatedAt = linkObj
									.getString(StringConstants.UPDATED_AT);
							sharedPreferencesEditor.putString(
									AppConstants.LAST_BOOKMARK_UPDATED_AT,
									updatedAt);

							sharedPreferencesEditor.commit();
							bookmarksAdapter.notifyDataSetChanged();
							Log.i(TAG,
									"Saving the updated time of first and last received bookmark");
						}						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

		}).execute();

	}

	private void startAuthorize() {
		(new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return mOauthService.getAuthorizationUrl(EMPTY_TOKEN);
			}

			@Override
			protected void onPostExecute(String url) {
				mWebView.loadUrl(url);
			}
		}).execute();
	}

	private WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (null != url) {
				if (url.equals(URLConstants.BASE_URL)
						|| url.equals(URLConstants.BASE_URL + "/")) {
					auth_dialog.dismiss();
					startAuthorize();
				} else if (url.startsWith(URLConstants.CALLBACK_URL)) {
					// Override webview when user came back to CALLBACK_URL
					mWebView.stopLoading();
					mWebView.setVisibility(View.INVISIBLE); // Hide webview if
															// necessary
					String authorizationCode = url
							.substring(URLConstants.CALLBACK_URL.length());
					final Verifier verifier = new Verifier(authorizationCode);
					(new AsyncTask<Void, Void, Token>() {
						@Override
						protected Token doInBackground(Void... params) {
							Token token = mOauthService.getAccessToken(
									mRequestToken, verifier);
							SharedPreferences.Editor editor = getActivity()
									.getPreferences(getActivity().MODE_PRIVATE)
									.edit();
							editor.putString(AppConstants.ACCESS_TOKEN_KEY,
									token.getToken());
							editor.putString(AppConstants.ACCESS_TOKEN_SECRET,
									token.getSecret());
							editor.commit();
							Log.i(TAG, "Saving the token key.");
							return token;
						}

						@Override
						protected void onPostExecute(final Token accessToken) {
							auth_dialog.dismiss();
							fetchBookmarks(accessToken,
									BookmarkLoadType.TIMELINE);
						}
					}).execute();
				}
			} else {
				super.onPageStarted(view, url, favicon);
			}
		}
	};

}
