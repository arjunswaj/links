module SearchesHelper
  def bookmarks_tag_searcher(time, keyword, user_id)
    tagname = keyword[1, keyword.length].strip.gsub(' ', '-').downcase
      @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: user_id, membership_status: true)    
      .where("bookmarks.updated_at < :now", now: time)
      .where("LOWER(tags.tagname) = LOWER(:tag)", tag: "#{tagname}")
      .order('bookmarks.updated_at DESC')    
      .limit(20) 
  end

  def bookmarks_searcher(time, keyword, user_id)
    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: user_id, membership_status: true)
      .where("bookmarks.updated_at < :now", now: time)
      .where("LOWER(bookmarks.title) LIKE LOWER(:query) OR LOWER(bookmarks.description) LIKE LOWER(:query)", query: "%#{keyword}%")      
      .order('bookmarks.updated_at DESC') 
      .limit(20)
  end

  def bookmarks_in_groups_tag_searcher(time, keyword, group_id)
    tagname = keyword[1, keyword.length].strip.gsub(' ', '-').downcase
      @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("bookmarks.group_id = ?", group_id)        
      .where("bookmarks.updated_at < :now", now: time)            
      .where("LOWER(tags.tagname) = LOWER(:tag)", tag: "#{tagname}")
      .order('bookmarks.updated_at DESC')    
      .limit(20) 
  end

  def bookmarks_in_group_searcher(time, keyword, group_id)
    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("bookmarks.group_id = ?", group_id)        
      .where("bookmarks.updated_at < :now", now: time)                  
      .where("LOWER(bookmarks.title) LIKE LOWER(:query) OR LOWER(bookmarks.description) LIKE LOWER(:query)", query: "%#{keyword}%")      
      .order('bookmarks.updated_at DESC') 
      .limit(20)
  end
end
