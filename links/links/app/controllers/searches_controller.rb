class SearchesController < ApplicationController
  include SearchesHelper
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
       bookmarks_tag_searcher(Time.now, keyword, current_user.id)
    else
       bookmarks_searcher(Time.now, keyword, current_user.id)
    end            

    bookmark = @bookmarks.last
    session[:last_link_time] = bookmark.updated_at
    session[:search_criteria] = keyword

    respond_to do |format|    
      format.js
    end   
  end

  def searchmore
    keyword = session[:search_criteria]

    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    if keyword.start_with?('#')
      bookmarks_tag_searcher(session[:last_link_time], keyword, current_user.id)
    else
      bookmarks_searcher(session[:last_link_time], keyword, current_user.id)      
    end            

    bookmark = @bookmarks.last
    if bookmark
      session[:last_link_time] = bookmark.updated_at    
      session[:search_criteria] = keyword
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
