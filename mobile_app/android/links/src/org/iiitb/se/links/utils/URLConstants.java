package org.iiitb.se.links.utils;

public class URLConstants {
  public static final String BASE_URL = "http://links.t.proxylocal.com";
  public static final String CALLBACK_URL = BASE_URL + "/oauth/authorize/";
  public static final String TIMELINE = BASE_URL + "/api/timeline";
  public static final String LOAD_MORE_BOOKMARKS = BASE_URL + "/api/loadmore";

  public static final String SEARCH = BASE_URL
      + "/api/searches/search_bookmark";
  public static final String SEARCH_MORE_BOOKMARKS = BASE_URL
      + "/api/searches/searchmore";

  public static final String SAVE_BOOKMARK = BASE_URL + "/api/savebookmark";

  public static final String DELETE_BOOKMARK = BASE_URL + "/api/deletebookmark";
  
  public static final String SHARE_BOOKMARK = BASE_URL + "/api/sharebookmark";
  
  public static final String SUBSCRIBED_GROUPS_INDEX = BASE_URL
      + "/api/groups/index";

  public static final String REQUESTS_GROUPS_INDEX = BASE_URL
      + "/api/groups/requests";

  public static final String UNSUBSCRIBE_GROUP = BASE_URL
      + "/api/groups/unsubscribe";

  public static final String ACCEPT_SUBSCRIBE = BASE_URL + "/api/groups/accept";
  public static final String REJECT_SUBSCRIBE = BASE_URL + "/api/groups/reject";
}
