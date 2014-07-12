module BookmarksHelper	
	def bookmarks_loader(time, user_id)		
	    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
	    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
	      .eager_load(group: :memberships)
	      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: user_id, membership_status: true)
	      .where("bookmarks.updated_at < :now", now: time)
	      .order('bookmarks.updated_at DESC')
	      .limit(20)
  	end
end
