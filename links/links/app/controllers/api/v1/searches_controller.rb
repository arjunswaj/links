module Api 
	module V1		
		class SearchesController < ApplicationController
			include SearchesHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json

			def search_bookmark
			    keyword = params[:keyword]
			    if keyword.start_with?('#')
			       bookmarks_tag_searcher(Time.now, keyword, doorkeeper_token.resource_owner_id)
			    else
			       bookmarks_searcher(Time.now, keyword, doorkeeper_token.resource_owner_id)
			    end            			    
			    bookmarks_formatter 
			end

			def searchmore
			    keyword = params[:keyword]
			    time = Time.at(params[:time].to_i).to_datetime

			    if keyword.start_with?('#')
			      bookmarks_tag_searcher(time, keyword, doorkeeper_token.resource_owner_id)
			    else
			      bookmarks_searcher(time, keyword, doorkeeper_token.resource_owner_id)
			    end            
			    bookmarks_formatter 
			end


			private

			def bookmarks_formatter
				formatted_bookmarks = []
				@bookmarks.each do  |bookmark|
				  formatted_tags = []
				  bookmark.tags.each do |tag|
				    formatted_tags << tag.tagname
				  end			  
				  formatted_bookmarks << {:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :updated_at => bookmark.updated_at.to_i, :tags => formatted_tags}
				end
				respond_with formatted_bookmarks
			end

		end
	end
end