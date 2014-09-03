module Api 
	module V1				
		# Controller for handling all operations related to Bookmarks via REST API
		class BookmarksController < ApplicationController
			include BookmarksHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json
			# Loads the bookmarks for the timeline
			# Formats the bookmarks into a JSON
			# This is paginated
			def timeline
				bookmarks_loader(Time.now, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter				
			end

			# Loads more bookmarks for the timeline
			# Formats the bookmarks into a JSON
			# This is paginated
			def loadmore
				time = Time.at(params[:time].to_i).to_datetime
				bookmarks_loader(time, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter
			end

			# Loads all the bookmarks for the timeline
			# Formats the bookmarks into a JSON
			# This is NOT paginated, Use paginated versions only
			def index
				bookmarks = Bookmark.where("user_id == ?", doorkeeper_token.resource_owner_id)
				formatted_bookmarks = []
				bookmarks.each do  |bookmark|
				  formatted_tags = []
				  bookmark.tags.each do |tag|
				    formatted_tags << tag.tagname
				  end			  
				  formatted_bookmarks << {:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :tags => formatted_tags}
				end
				respond_with formatted_bookmarks
			end

			
			# Persists the Bookmark as follows:
			# * Saves the URL if not present
			# * Saves the bookmark object
			# * Finds the Tags, if not present, will save the object
			# * If Group id is set, will associate the bookmark with the Group and saves it
			# Handles Async requsts
			def savebookmark
				url_str = save_bookmark_params[:url]
				annotations = get_annotations(url_str)
				url = Url.find_by_url(url_str)
			    if url.nil?
			      url = Url.new({:url => save_bookmark_params[:url], :icon => annotations[:icon]})
			      if !url.save
			        render :status => 404
			      end
			    else
			        Url.update(url.id, :icon => annotations[:icon]) if url.icon.nil? && annotations[:icon] != ''
			    end		    
			    @bookmark = Bookmark.new({:title => save_bookmark_params[:title], :description => save_bookmark_params[:description], :url => url, :user_id => doorkeeper_token.resource_owner_id})			    

			    tags = save_bookmark_params[:tags].split(",")
			    tags.each do |tag|
			      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
			        @tag = Tag.new
			        @tag.tagname = tag.strip.gsub(' ','-').downcase
			      @bookmark.tags << @tag
			      else
			        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
			      end
			    end unless tags.nil?			    
			    
			    if @bookmark.save
			    	respond_to do |format|
  						format.json{ render :json => strip_bookmark_to_json(@bookmark).to_json }
					end			        
			    else
			       format.json { render json: @bookmark.errors, status: :unprocessable_entity }
			    end			    
			end		

			# Updates the Bookmark as follows:
			# * Saves the new URL if not present
			# * Updates the bookmark object
			# * Finds the Tags, if not present, will save/update the object
			# * If Group id is set, will associate the bookmark with the Group and updates it
			# Handles Async requsts
			def updatebookmark
				url_str = update_bookmark_params[:url]
				annotations = get_annotations(url_str)
				url = Url.find_by_url(url_str)		

			    url = Url.find_by_url(update_bookmark_params[:url])
			    if url.nil?
			      url = Url.new({:url => update_bookmark_params[:url], :icon => annotations[:icon]})
			      if !url.save
			        format.json { render status: :unprocessable_entity }
			      end
			    else
			        Url.update(url.id, :icon => annotations[:icon]) if url.icon.nil? && annotations[:icon] != ''
			    end	

			    @bookmark = Bookmark.find_by_id(update_bookmark_params[:bookmark_id])
			    if @bookmark.nil?
			      format.json { render status: :unprocessable_entity }
			    end
			    @bookmark.title = update_bookmark_params[:title]
			    @bookmark.description = update_bookmark_params[:description]
			    @bookmark.url = url
			    @bookmark.user_id = doorkeeper_token.resource_owner_id

			    @bookmark.tags.clear

			    tags = update_bookmark_params[:tags].split(",")
			    tags.each do |tag|
			      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
			        @tag = Tag.new
			        @tag.tagname = tag.strip.gsub(' ','-').downcase
			      @bookmark.tags << @tag
			      else
			        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
			      end
			    end unless tags.nil?

			    
		      	if @bookmark.save
		        	respond_to do |format|
						format.json{ render :json => strip_bookmark_to_json(@bookmark).to_json }
					end		     
		       	else			        
		        	 format.json { render json: @bookmark.errors, status: :unprocessable_entity }
		       	end
			end

			# Deletes the bookmarks based on the bookmark id
			def deletebookmark
				bookmark = Bookmark.find_by_id(params[:id])
				if bookmark.user_id == doorkeeper_token.resource_owner_id
					bookmark.destroy
					head :ok
				else
					render :status => 504
				end
			end	

			# Shares the bookmarks to set of groups
			# Params to be passed:
			# * bookmark id of the bookmark
			# * array of group ids to which it has to be shared
			def share_bookmark_to_groups
			    bookmark_to_share = Bookmark.find(share_to_group_params['bookmark_id'])
			    group_ids = share_to_group_params['group_ids']
			    puts group_ids.to_s
			    @bookmarks = Array.new
			    group_ids.each do |group_id|
			      bookmark_to_save = Bookmark.new({:title => bookmark_to_share.title,
			        :description => bookmark_to_share.description,
			        :url => bookmark_to_share.url,
			        :user_id => doorkeeper_token.resource_owner_id,
			        :group_id => group_id,
			        :tags => bookmark_to_share.tags})
			      if !bookmark_to_save.save
			        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }
			      end
			      @bookmarks << bookmark_to_save
			    end	
			    head :ok
			end

			private

			# Formats the bookmarks to a JSON
			def bookmarks_formatter
				formatted_bookmarks = []
				@bookmarks.each do  |bookmark|
					formatted_bookmarks << strip_bookmark_to_json(bookmark)
				end
				respond_with formatted_bookmarks
			end

			# Strips the unwanted data off the bookmarks object
			# Params:
			# +bookmark+:: The bookmark object from ActiveModel
			def strip_bookmark_to_json(bookmark)
				formatted_tags = []
				bookmark.tags.each do |tag|
				   formatted_tags << tag.tagname
				end			  				
				bookmark_json = {:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :updated_at => bookmark.updated_at.to_i, :tags => formatted_tags}

				if bookmark.group_id
					bookmark_json[:username] = bookmark.user.name
					bookmark_json[:groupname] = bookmark.group.name	
				end
				
				if bookmark.user_id == doorkeeper_token.resource_owner_id
					bookmark_json[:my_bookmark] = true
				else
					bookmark_json[:my_bookmark] = false
				end			
				bookmark_json
			end
			
			# Util to get bookmark params while saving
			def save_bookmark_params
			    params.permit(:url, :title, :description, :tags)
			end
			
			# Util to get bookmark params while updating
			def update_bookmark_params
			    params.permit(:bookmark_id, :url, :title, :description, :tags)
			end
			
			# Util to get bookmark params while sharing to groups
			def share_to_group_params
			    params.permit(:bookmark_id, :group_ids => [])
			end					
		end
	end
end
