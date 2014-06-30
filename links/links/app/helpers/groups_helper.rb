module GroupsHelper
	def group_bookmarks_loader(time, group_id)			    
	    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
	      .eager_load(group: :memberships)
      	  .where("bookmarks.group_id = ?", group_id)	      
	      .where("bookmarks.updated_at < :now", now: time)
	      .order('bookmarks.updated_at DESC')
	      .limit(10)
  	end
end
