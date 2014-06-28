module Api 
	module V1		
		class GroupsController < ApplicationController
			include GroupsHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json

			def index
			    @all_groups = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , doorkeeper_token.resource_owner_id, true)
			    groups_formatter
			end		

			private

			def groups_formatter
				formatted_groups = []
				@all_groups.each do  |group|				  				  
				  formatted_groups << {:id => group.id, :name => group.name}
				end
				respond_with formatted_groups
			end

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