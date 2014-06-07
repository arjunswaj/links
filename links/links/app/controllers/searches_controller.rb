class SearchesController < ApplicationController
  def index
  end

  def search_user
  	email = user_search_params['query']  	
  	@users = User.where("email LIKE :prefix", prefix: "#{email}%")
  	respond_to do |format|    	
  		user_json = @users.to_json(:only => [ :id, :email ])
    	format.json  { render :json => user_json } # don't do msg.to_json
  	end  	
  end

  def search_bookmark
    keyword = link_search_params['keyword']  
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    if keyword.start_with?('#')
      tagname = keyword[1, keyword.length].strip.gsub(' ', '-').downcase
      @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: "#{current_user.id}", membership_status: "t")    
      .where("LOWER(tags.tagname) = LOWER(:tag)", tag: "#{tagname}")
      .order('bookmarks.updated_at DESC')      
    else
      @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: "#{current_user.id}", membership_status: "t")
      .where("LOWER(title) LIKE LOWER(:query) OR LOWER(description) LIKE LOWER(:query)", query: "%#{keyword}%")      
      .order('bookmarks.updated_at DESC') 
    end            

    respond_to do |format|    
      format.js
    end   
  end

  def receiver  	
  	@user_email = user_receiver_params['users']  	
  end


  private

  def user_receiver_params
	params.permit(:users => [])
  end

  def user_search_params
    params.permit(:query)
  end

  def link_search_params
    params.permit(:keyword)
  end
end
