package org.iiitb.se.links.home.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardView;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment that appears in the "content_frame", shows Links
 */
public class LinkFragment extends Fragment {
	public static final String LINK_OPTION_NUMBER = "link_option_number";
	private WebView mWebView;
	private TextView tv;
	private Dialog auth_dialog;
	private OAuthService mOauthService;
	private Token mRequestToken;
	protected ScrollView mScrollView;

	private static final Token EMPTY_TOKEN = null;
	private static final String TAG = "LinkFragment";

	public LinkFragment() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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

		tv = (TextView) rootView.findViewById(R.id.result);
		mOauthService = new ServiceBuilder().provider(LinksApi.class)
				.apiKey(AppConstants.API_KEY)
				.apiSecret(AppConstants.API_SECRET)
				.callback(AppConstants.URN_IETF_WG_OAUTH_2_0_OOB).build();

		SharedPreferences sharedPreferences = getActivity().getPreferences(
				getActivity().MODE_PRIVATE);
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
			fetchBookmarks(accessToken);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mScrollView = (ScrollView) getActivity().findViewById(
				R.id.card_scrollview);

		initCards();
	}

	private void initCards() {
		init_standard_header_with_overflow_button_dynamic_menu_without_xml();
	}

	private void fetchBookmarks(final Token accessToken) {
		(new AsyncTask<Void, Void, String>() {
			Response response;
			int status;

			@Override
			protected String doInBackground(Void... params) {
				OAuthRequest request = new OAuthRequest(Verb.GET,
						URLConstants.PROTECTED_RESOURCE_URL_BOOKMARKS);
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
					tv.setText(responseBody);
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
							fetchBookmarks(accessToken);
						}
					}).execute();
				}
			} else {
				super.onPageStarted(view, url, favicon);
			}
		}
	};

	private void init_standard_header_with_overflow_button_dynamic_menu_without_xml() {

		// Create a Card
		Card card = new Card(getActivity());

		// Create a CardHeader
		CardHeader header = new CardHeader(getActivity());

		// Set the header title
		header.setTitle("LOL");

		// Add a popup menu. This method set OverFlow button to visible
		header.setButtonOverflowVisible(true);
		header.setPopupMenuListener(new CardHeader.OnClickCardHeaderPopupMenuListener() {
			@Override
			public void onMenuItemClick(BaseCard card, MenuItem item) {
				Toast.makeText(
						getActivity(),
						"Click on " + item.getTitle() + "-"
								+ ((Card) card).getCardHeader().getTitle(),
						Toast.LENGTH_SHORT).show();
			}
		});

		// Add a PopupMenuPrepareListener to add dynamically a menu entry
		// it is optional.
		header.setPopupMenuPrepareListener(new CardHeader.OnPrepareCardHeaderPopupMenuListener() {
			@Override
			public boolean onPreparePopupMenu(BaseCard card, PopupMenu popupMenu) {
				popupMenu.getMenu().add("Dynamic item");

				// You can remove an item with this code
				// popupMenu.getMenu().removeItem(R.id.action_settings);

				// return false; You can use return false to hidden the button
				// and the popup

				return true;
			}
		});
		card.addCardHeader(header);

		// Set card in the cardView
		CardView cardView = (CardView) getActivity().findViewById(
				R.id.carddemo_header_overflow_dynamic2);
		cardView.setCard(card);
	}
}
