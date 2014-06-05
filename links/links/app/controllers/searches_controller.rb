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
      @bookmarks = Bookmark.joins(:tags, :user).where("LOWER(tags.tagname) = LOWER(:tag)", tag: "#{tagname}")
      .where("users.id = :user_id", user_id: "#{current_user.id}")
      .order('updated_at DESC')      
    else
      @bookmarks = current_user.bookmarks.where("LOWER(title) LIKE LOWER(:query) OR LOWER(description) LIKE LOWER(:query)", 
      query: "%#{keyword}%").order('updated_at DESC')
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
