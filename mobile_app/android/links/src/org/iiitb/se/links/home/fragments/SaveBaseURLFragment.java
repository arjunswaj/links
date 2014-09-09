package org.iiitb.se.links.home.fragments;

import org.iiitb.se.links.R;
import org.iiitb.se.links.utils.AppConstants;
import org.iiitb.se.links.utils.URLConstants;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
public class SaveBaseURLFragment extends Fragment {
  private static final String TAG = "SaveBaseURLFragment";
  protected SharedPreferences.Editor sharedPreferencesEditor;
  protected SharedPreferences sharedPreferences;
  protected ProgressDialog mProgressDialog;
  private EditText url;
  private Button ok;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.save_base_url, container, false);

    url = (EditText) rootView.findViewById(R.id.base_url_text);
    sharedPreferences = getActivity().getPreferences(Activity.MODE_PRIVATE);
    String BASE_URL = sharedPreferences.getString(AppConstants.BASE_URL, null);
    if (null != BASE_URL) {
      url.setText(BASE_URL);
    }
    ok = (Button) rootView.findViewById(R.id.ok);

    getActivity().setTitle(getString(R.string.save_base_url));

    mProgressDialog = new ProgressDialog(getActivity());

    mProgressDialog.setMessage(getActivity().getString(R.string.loading));
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDialog.setCanceledOnTouchOutside(false);
    mProgressDialog.setCancelable(false);

    ok.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        mProgressDialog.show();
        sharedPreferencesEditor = getActivity().getPreferences(
            Activity.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(AppConstants.BASE_URL, url.getText()
            .toString().trim());
        sharedPreferencesEditor.commit();
        Log.i(TAG, "Base URL is now saved. Hopefully you should access this!");
        closeThisFragmentAndLoadHome();
        mProgressDialog.hide();
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
  }

  public SaveBaseURLFragment() {
    // Empty constructor required for fragment subclasses
  }
}
