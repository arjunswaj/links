module SearchesHelper
  def bookmarks_tag_searcher(time, keyword, user_id)
    tagname = keyword[1, keyword.length].strip.gsub(' ', '-').downcase
      @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: user_id, membership_status: "t")    
      .where("bookmarks.updated_at < :now", now: time)
      .where("LOWER(tags.tagname) = LOWER(:tag)", tag: "#{tagname}")
      .order('bookmarks.updated_at DESC')    
      .limit(5) 
  end

  def bookmarks_searcher(time, keyword, user_id)
    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: user_id, membership_status: "t")
      .where("bookmarks.updated_at < :now", now: time)
      .where("LOWER(title) LIKE LOWER(:query) OR LOWER(description) LIKE LOWER(:query)", query: "%#{keyword}%")      
      .order('bookmarks.updated_at DESC') 
      .limit(5)
  end
end
