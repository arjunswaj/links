module Api 
	module V1
		class BookmarksController < ApplicationController
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json

			def index
				respond_with Bookmark.where("user_id == ?", doorkeeper_token.resource_owner_id)
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
		end
	end
end
