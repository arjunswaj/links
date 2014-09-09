package org.iiitb.se.links.utils;

public class URLConstants {

  public static final String CALLBACK_URL = "/oauth/authorize/";
  public static final String TIMELINE = "/api/timeline";
  public static final String LOAD_MORE_BOOKMARKS = "/api/loadmore";

  public static final String SEARCH = "/api/searches/search_bookmark";
  public static final String SEARCH_MORE_BOOKMARKS = "/api/searches/searchmore";

  public static final String SEARCH_IN_GROUP = "/api/searches/groups/%s/search_bookmark/%s";
  public static final String SEARCH_MORE_BOOKMARKS_IN_GROUP = "/api/searches/groups/%s/searchmore/%s/%s";

  public static final String SAVE_BOOKMARK = "/api/savebookmark";

  public static final String SAVE_BOOKMARK_IN_GROUPS = "/api/groups/savebookmark";

  public static final String UPDATE_BOOKMARK = "/api/updatebookmark";

  public static final String DELETE_BOOKMARK = "/api/deletebookmark";

  public static final String SHARE_BOOKMARK = "/api/sharebookmark";

  public static final String SUBSCRIBED_GROUPS_INDEX = "/api/groups/index";

  public static final String REQUESTS_GROUPS_INDEX = "/api/groups/requests";

  public static final String UNSUBSCRIBE_GROUP = "/api/groups/unsubscribe";

  public static final String ACCEPT_SUBSCRIBE = "/api/groups/accept";
  public static final String REJECT_SUBSCRIBE = "/api/groups/reject";

  public static final String GROUPS_TIMELINE = "/api/groups/%s/timeline";
  public static final String GROUPS_LOAD_MORE_BOOKMARKS = "/api/groups/%s/loadmore/%s";

  public static final String LOGOUT = "/oauth/authorize";
}
