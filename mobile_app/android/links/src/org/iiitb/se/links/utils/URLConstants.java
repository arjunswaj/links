package org.iiitb.se.links.utils;

public class URLConstants {
  public static final String BASE_URL = "http://links.t.proxylocal.com";
  public static final String CALLBACK_URL = BASE_URL + "/oauth/authorize/";
  public static final String TIMELINE = BASE_URL
      + "/api/timeline";
  public static final String LOAD_MORE_BOOKMARKS = BASE_URL
	      + "/api/loadmore";
  
  public static final String SEARCH = BASE_URL
      + "/api/searches/search_bookmark";
  public static final String SEARCH_MORE_BOOKMARKS = BASE_URL
      + "/api/searches/searchmore";
  
  public static final String SAVE_BOOKMARK = BASE_URL
      + "/api/savebookmark";
}
