module Api 
	module V1		
		class BookmarksController < ApplicationController
			include BookmarksHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json
			def timeline
				bookmarks_loader(Time.now, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter				
			end

			def loadmore
				time = Time.at(params[:time].to_i).to_datetime
				bookmarks_loader(time, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter
			end

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

			def savebookmark
				url = Url.find_by_url(save_bookmark_params[:url])
			    if url.nil?
			      url = Url.new({:url => save_bookmark_params[:url]})
			      if !url.save
			        render :status => 404
			      end
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
			       render :status => 404
			    end			    
			end		

			def deletebookmark
				bookmark = Bookmark.find_by_id(params[:id])
				if bookmark.user_id == doorkeeper_token.resource_owner_id
					bookmark.destroy
					head :ok
				else
					render :status => 504
				end
			end	

			private

			def bookmarks_formatter
				formatted_bookmarks = []
				@bookmarks.each do  |bookmark|
					formatted_bookmarks << strip_bookmark_to_json(bookmark)
				end
				respond_with formatted_bookmarks
			end

			def strip_bookmark_to_json(bookmark)
				formatted_tags = []
				bookmark.tags.each do |tag|
				   formatted_tags << tag.tagname
				end			  
				{:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :updated_at => bookmark.updated_at.to_i, :tags => formatted_tags}
				
			end
			def save_bookmark_params
			    params.permit(:url, :title, :description, :tags)
			end
		end
	end
end
