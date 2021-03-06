module Api 
	module V1		
		# Controller for handling all operations related to Groups via REST API
		class GroupsController < ApplicationController
			include GroupsHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json

			# Loads the groups
			# This is paginated
			def timeline
				group_bookmarks_loader(Time.now, params[:id]) 
				bookmarks_formatter				
			end
			
			# Loads more groups
			# This is paginated
			def loadmore
				time = Time.at(params[:time].to_i).to_datetime
				group_bookmarks_loader(time, params[:id]) 
				bookmarks_formatter
			end

			# Loads all the groups
			def index
			    all_groups = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , doorkeeper_token.resource_owner_id, true)
			    groups_formatter(all_groups)
			end		
			# Loads all the group requests
			def requests
			    invites = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , doorkeeper_token.resource_owner_id, false) 
			    groups_formatter(invites)
			end

			# Accepts the invite to a group
			def accept_invite			    			    
			    membership = Membership.find_by_group_id_and_user_id(params[:group_id], doorkeeper_token.resource_owner_id)
			    unless membership.nil?
			      membership.update_attributes :acceptance_status => true
			      respond_to do |format|			        
			        format.json { head :no_content }
			      end
			    else
			      respond_to do |format|			        
			        format.json { render json: "You have not been invited to the group", status: :forbidden}
			      end
			    end
			end
			
			# Rejects the invite to a group
			def reject_invite						    			   
			    membership = Membership.find_by_group_id_and_user_id(params[:group_id], doorkeeper_token.resource_owner_id)
			    unless membership.nil?
			      membership.destroy
			      respond_to do |format|			        
			        format.json { head :no_content }
			      end
			    else
			      respond_to do |format|			        
			        format.json { render json: "You have not been invited to the group", status: :forbidden}
			      end
			    end				
			end

			# Unsubscribe the subscription to a group
			def unsubscribe
				group_id = params[:group_id]
			    if ((!group_owner? doorkeeper_token.resource_owner_id, group_id) && (group_member? doorkeeper_token.resource_owner_id, group_id))
			      set_group(group_id)
			      user = User.find_by_id(doorkeeper_token.resource_owner_id)
			      user.groups.delete(@group)
			      respond_to do |format|			        
			        format.json { head :no_content }
			      end
			    else
			      respond_to do |format|			        
			        format.json { render json: "Only members of a group can unsubscribe from the group. The group owner cannot subscribe from the group", status: :method_not_allowed }
			      end
			    end
   		    end

			# Saves a bookmark in a group as follows:
			# * Saves the URL if not present
			# * Saves the bookmark object
			# * Finds the Tags, if not present, will save the object
			# * Will associate the bookmark with the Group and saves it
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
			    group_ids = save_bookmark_params['group_ids']
			    @bookmarks = Array.new

			    group_ids.each do |group_id|		    
				    @bookmark = Bookmark.new({:title => save_bookmark_params[:title], :description => save_bookmark_params[:description], :url => url, :user_id => doorkeeper_token.resource_owner_id, :group_id => group_id})			    

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
				    	@bookmarks << @bookmark
				    else
				       format.json { render json: @bookmark.errors, status: :unprocessable_entity }
				    end	
				end	
				head :ok
			end

			private

			# Util to get bookmark params
			def save_bookmark_params
			    params.permit(:url, :title, :description, :tags, :group_ids => [])
			end

			# Converts Groups to JSON
			def groups_formatter(groups)
				formatted_groups = []
				groups.each do  |group|				  				  
				  formatted_groups << {:id => group.id, :name => group.name, :description => (group.description != nil) ? group.description : ""}
				end
				respond_with formatted_groups
			end			

			# Formats the bookmarks into a JSON
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

			# Identifies if the user is group owner or not
			# Params:
			# +user_id+:: User id to be checked
			# +group_id+:: Group id to be checked
			def group_owner?(user_id, group_id)
			    return !Group.where("owner_id = ? and id = ?", user_id, group_id).empty?
			end

			# Loads group object based on group id
			# Params:
			# +group_id+:: ID of group that has to be loaded
			def set_group(group_id)
			    begin
			      @group = Group.find(group_id)
			    rescue ActiveRecord::RecordNotFound
			      #render :file => "public/404.html", :status => :unauthorized, :layout => false
			      redirect_to groups_path, alert: "Specified group, #{group_id}, does not exist"
			    end
			end

			# Identifies if the user is group member or not
			# Params:
			# +user_id+:: User id to be checked
			# +group_id+:: Group id to be checked
			def group_member?(user_id, group_id)
			    return !Membership.where("user_id = ? and group_id = ? and acceptance_status = ?", user_id, group_id, true).empty?
			end

		end
	end
end