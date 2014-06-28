module Api 
	module V1		
		class GroupsController < ApplicationController
			include GroupsHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json

			def index
			    all_groups = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , doorkeeper_token.resource_owner_id, true)
			    groups_formatter(all_groups)
			end		

			def requests
			    invites = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , doorkeeper_token.resource_owner_id, false) 
			    groups_formatter(invites)
			end

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


			private

			def groups_formatter(groups)
				formatted_groups = []
				groups.each do  |group|				  				  
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

			def group_owner?(user_id, group_id)
			    return !Group.where("user_id = ? and id = ?", user_id, group_id).empty?
			end

			def set_group(group_id)
			    begin
			      @group = Group.find(group_id)
			    rescue ActiveRecord::RecordNotFound
			      #render :file => "public/404.html", :status => :unauthorized, :layout => false
			      redirect_to groups_path, alert: "Specified group, #{group_id}, does not exist"
			    end
			end

			def group_member?(user_id, group_id)
			    return !Membership.where("user_id = ? and group_id = ? and acceptance_status = ?", user_id, group_id, true).empty?
			end

		end
	end
end