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

			def show
				respond_with Bookmark.find(params[:id])
			end

			def create
				bookmark = { :url_attributes => {:url => params[:url]}, :title => params[:title], :description => params[:description], :user_id => doorkeeper_token.resource_owner_id}
				respond_with Bookmark.create(bookmark)
			end

			def update
				bookmark = { :url_attributes => {:url => params[:url]}, :title => params[:title], :description => params[:description], :user_id => doorkeeper_token.resource_owner_id}
				respond_with Bookmark.update(params[:id], bookmark)
			end

			def destroy
				respond_with Bookmark.destroy(params[:id])
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
