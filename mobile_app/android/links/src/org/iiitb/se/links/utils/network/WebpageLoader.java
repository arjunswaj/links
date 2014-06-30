package org.iiitb.se.links.utils.network;

import java.io.IOException;

import org.iiitb.se.links.utils.StringConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scribe.model.Token;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;

public class WebpageLoader extends AbstractResourceDownloader {

  private static final String TAG = "WebpageLoader";
  private AddBookmarkFormElements addBookmarkFormElements;

  public interface AddBookmarkFormElements {
    public EditText getUrl();
    public EditText getTitle();
    public EditText getDescription();
    public EditText getTags();  
    public void hideKeyboard();
  }
  
  public WebpageLoader(Context context, AddBookmarkFormElements addBookmarkFormElements) {
    super(context);
    this.addBookmarkFormElements = addBookmarkFormElements;
  }

  @Override
  public void fetchProtectedResource(Token accessToken) {
    // NO-OP
  }

  public void fetchWebPageDetails(final boolean setOnlyTags) {
    if (netAvailable()) {
      (new AsyncTask<Void, Integer, Document>() {
        @Override
        protected void onPreExecute() {
          mProgressDialog.show();
        }

        @Override
        protected Document doInBackground(Void... params) {
          Document doc = null;
          try {
            doc = Jsoup.connect(
                addBookmarkFormElements.getUrl().getText().toString()).get();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
          if (null != doc) {
            if (!setOnlyTags) {
              addBookmarkFormElements.getTitle().setText(doc.title());
            }
            Elements metas = doc.select(StringConstants.META);
            for (Element meta : metas) {
              if (meta.attr(StringConstants.NAME).equals(
                  StringConstants.DESCRIPTION)) {
                addBookmarkFormElements.getDescription().setText(
                    meta.attr(StringConstants.CONTENT));
              }

              if (meta.attr(StringConstants.NAME).equals(
                  StringConstants.KEYWORDS)) {
                addBookmarkFormElements.getTags().setText(
                    meta.attr(StringConstants.CONTENT));
              }
            }
          }
          mProgressDialog.hide();
          addBookmarkFormElements.hideKeyboard();
        }

      }).execute();
    }
  }

}
