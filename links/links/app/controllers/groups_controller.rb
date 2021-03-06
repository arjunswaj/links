# This controller contains all group related actions
class GroupsController < ApplicationController
  # Authorization through 'devise'
  before_action :authenticate_user!
  
  #TODO: json response
  
  # GET /groups
  # All groups of which the authenticated user is a member of
  def index
    @all_groups = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , current_user, true)
  end
    
  # GET /owned_groups
  # All groups which the authenticated user owns
  def owned_groups
    @owned_groups = Group.where("owner_id = ?", current_user)
  end

  # Groups to which the specified user has been invited
  def self.group_invites(user)
    Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , user, false)
  end
  
  # GET /group_invites
  # Groups to which the the authenticated user has been invited
  def group_invites
    @invites = GroupsController.group_invites current_user
  end
  
  def shareable_groups
    index
    owned_groups
    @bookmark_id = share_params['bookmark_id']
    respond_to do |format|
      format.js
    end
  end

  # GET /groups/1
  # GET /groups/1.json
  # Details of the specified group
  def show
    set_group

    # For groups timeline
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']    
    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("bookmarks.group_id IS NOT NULL")
      .where("bookmarks.group_id = ?", params[:id])
      .order('bookmarks.updated_at DESC')
  end
  
  # GET /groups/1
  # Members of the specified group
  def members
    set_group    
    @members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", params[:id], true) if GroupsController.group_member? current_user.id, params[:id]
  end

  # Members who still haven't responded to invites to the specfied group
  def self.pending_members(group_id, owner_id)
    pending_members = []
    pending_members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", group_id, false) if GroupsController.group_owner? owner_id, group_id
    return pending_members
  end
 
  # GET /pending_members/1
  # Members who still haven't responded to invites to the specified group
  def pending_members
    set_group    
    @pending_members = GroupsController.pending_members(params[:id], current_user.id)
  end

  # GET /groups/new
  # New group
  def new
    @group = Group.new
  end

  # GET /groups/1/edit
  # Edit specified group details
  def edit
    if GroupsController.group_owner? current_user.id, params[:id]
      set_group
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Can't Edit. You are not the group owner" }
        format.json { render json: "Only owners can edit the respective group page", status: :unauthorized }
      end
    end
  end

  # POST /groups
  # POST /groups.json
  # Create a new group with the specified details
  def create
    @group = Group.new(group_params)
    @group.owner = current_user
    @group.users << current_user

    respond_to do |format|
      if @group.save
        membership = Membership.find_by_group_id_and_user_id(@group.id, current_user)
        membership.update_attributes :acceptance_status => true

        format.html { redirect_to group_path(@group), alert: 'Group was successfully created.' }
        format.json { render action: 'show', status: :created, location: @group }
      else
        format.html { render action: 'new' }
        format.json { render json: @group.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /groups/1
  # PATCH/PUT /groups/1.json
  # Update the specified group with the given details
  def update
    if GroupsController.group_owner? current_user.id, params[:id]
      set_group
      respond_to do |format|
        if @group.update(group_params)
          format.html { redirect_to group_path(@group), alert: 'Group was successfully updated.' }
          format.json { head :no_content }
        else
          format.html { render action: 'edit' }
          format.json { render json: @group.errors, status: :unprocessable_entity }
        end
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Not updated. You are not the group owner" }
        format.json { render json: "Only owners can edit the respective group page", status: :unauthorized }
      end
    end
  end

  # DELETE /groups/1
  # DELETE /groups/1.json
  # Delete the specified group
  def destroy
    if GroupsController.group_owner? current_user.id, params[:id]
      set_group
      @group.destroy
      respond_to do |format|
        format.html { redirect_to groups_path, alert: 'Delete group successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Not deleted. You are not the group owner" }
        format.json { render json: "Only owners can delete the respective group", status: :unauthorized }
      end
    end
  end

  # POST /groups/1/invite_users
  # POST /groups/1/invite_users.json
  # Mark specified user as invited
  def invite_users
    if GroupsController.group_owner? current_user.id, params[:id]
      set_group
      params[:users].each do |email|
        user = User.find_by_email(email) # TODO: only possible because email is unique. Should make magic assist to return user id's
        @group.users << user unless @group.users.include? user or user.nil?
      end unless params[:users].nil?
      respond_to do |format|
        format.html { redirect_to group_path(@group), alert: 'Invited users successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Not invited. You are not the group owner" }
        format.json { render json: "Only owners can add users to the respective group", status: :unauthorized}
      end
    end
  end

  # PUT /groups/1/add_user/1
  # PUT /groups/1/add_user/1.json
  # Mark that the specified user is now a member of the specified group
  def accept_invite
    group = Group.find(params[:group_id])
    user = User.find(params[:user_id])
    membership = Membership.find_by_group_id_and_user_id(group, user)
    unless membership.nil?
      membership.update_attributes :acceptance_status => true
      respond_to do |format|
        format.html { redirect_to group_path(group), alert: "Now member of #{group.name}" }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Not accepted. You have not been invited to the group" }
        format.json { render json: "You have not been invited to the group", status: :forbidden}
      end
    end
  end

  # DELETE /groups/1/users/2
  # DELETE /groups/1/users/2.json
  # Remove the specified user from the membership of the specified group owned by the authenticated user
  def remove_user
    group = Group.find(params[:group_id])
    user = User.find(params[:user_id])
    if (GroupsController.group_owner? current_user.id, group.id) && (user != current_user)
      group.users.delete user
      respond_to do |format|
        format.html { redirect_to group_path(group), alert: 'Removed user successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to group_path(group), alert: "Not removed. You are not the owner of the group or you are attempting to remove the owner from the group"}
        format.json { render json: "Only owners can remove users from the group and owners themselves cannot be removed from the group", status: :unauthorized}
      end
    end
  end

  # POST /groups/1/unsubscribe
  # POST /groups/1/unsubscribe.json
  # The authenticated user is no longer the member of the specified group
  def unsubscribe
    if (!GroupsController.group_owner? current_user.id, params[:id]) && (GroupsController.group_member? current_user.id, params[:id])
      set_group
      current_user.groups.delete(@group)
      respond_to do |format|
        format.html { redirect_to groups_path, alert: 'Unsubscribed successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path, alert: "Not Unsubscribed. You are not a member of the group or you are the owner of the group" }
        format.json { render json: "Only members of a group can unsubscribe from the group. The group owner cannot subscribe from the group", status: :method_not_allowed }
      end
    end
  end

  # DELETE /groups/1/users/2/cancel
  # DELETE /groups/1/users/2/cancel.json
  # Authenticated user cancels invite of the specified user to the specified group
  def cancel_invite
    if (GroupsController.group_owner? current_user.id, params[:group_id])
      membership = Membership.find_by_user_id_and_group_id_and_acceptance_status(params[:user_id], params[:group_id], false)
      Membership.delete membership unless membership.nil?
      respond_to do |format|
        format.html { redirect_to group_path(Group.find(params[:group_id])), alert: 'Cancelled invitation successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to group_path(Group.find(params[:group_id])), alert: "Nothing to cancel. The user has not at all been invited."}
        format.json { render json: "Only invited members may be cancelled.", status: :method_not_allowed }
      end
    end
  end
  
  def self.group_owner?(owner_id, group_id)
    return !Group.where("owner_id = ? and id = ?", owner_id, group_id).empty?
  end
  
  def self.group_member?(user_id, group_id)
    return !Membership.where("user_id = ? and group_id = ? and acceptance_status = ?", user_id, group_id, true).empty?
  end

  private

  def set_group
    begin
      @group = Group.find(params[:id])
    rescue ActiveRecord::RecordNotFound
      redirect_to groups_path, alert: "Specified group, #{params[:id]}, does not exist"
    end
  end

  # Never trust parameters from the scary internet, only allow the white list through.
  def group_params
    params.require(:group).permit(:name, :description)
  end

  def user_receiver_params
    params.permit(:users => [])
  end

  def share_params
    params.permit(:bookmark_id)
  end

end