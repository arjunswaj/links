class GroupsController < ApplicationController
  before_action :authenticate_user!
  before_action :set_invites # The invites is planned to be shown in the left pane of every group page
  
  layout 'base'
  #TODO: json response
  
  # GET /groups
  def index
    @owner_of_groups = Group.where("user_id = ?", current_user)
    @member_of_groups = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , current_user, true)
  end

  def shareable_groups
    index
    @bookmark_id = share_params['bookmark_id']
    respond_to do |format|      
      format.js
    end
  end

  # GET /groups/1
  # GET /groups/1.json
  # if an invite comes, a user must be able to visit the group and check it out in general
  def show
    set_group
    
    @accepted_members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", params[:id], true)
    @pending_members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", params[:id], false)
    
    # For groups timeline
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    @bookmarks = Bookmark.where("group_id = ?", params[:id])

    if group_owner? params[:id]
      @group_owner = current_user
    elsif group_member? params[:id]
        @group_member = current_user
    else
      @invite = Membership.find_by_user_id_and_group_id_and_acceptance_status(current_user, @group, false) 
    end  
  end

  # GET /groups/new
  def new
    @group = Group.new
  end

  # GET /groups/1/edit
  def edit
    if group_owner? params[:id]
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
  def create
    @group = Group.new(group_params)
    @group.user = current_user
    @group.users << current_user

    respond_to do |format|
      if @group.save
        membership = Membership.find_by_group_id_and_user_id(@group.id, current_user)
        membership.update_attributes :acceptance_status => true

        format.html { redirect_to @group, notice: 'Group was successfully created.' }
        format.json { render action: 'show', status: :created, location: @group }
      else
        format.html { render action: 'new' }
        format.json { render json: @group.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /groups/1
  # PATCH/PUT /groups/1.json
  def update
    if group_owner? params[:id]
      set_group
      respond_to do |format|
        if @group.update(group_params)
          format.html { redirect_to @group, notice: 'Group was successfully updated.' }
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
  def destroy
    if group_owner? params[:id]
      set_group
      @group.destroy
      respond_to do |format|
        format.html { redirect_to groups_path, notice: 'Delete group successfully.' }
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
  def invite_users
    if group_owner? params[:id]
      set_group
      params[:users].each do |email|
        user = User.find_by_email(email) # TODO: only possible because email is unique. Should make magic assist to return user id's
        @group.users << user unless @group.users.include? user or user.nil?
      end unless params[:users].nil?
      respond_to do |format|
        format.html { redirect_to @group, notice: 'Invited users successfully.' }
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
  def accept_invite
    group = Group.find(params[:group_id])
    user = User.find(params[:user_id])
    membership = Membership.find_by_group_id_and_user_id(group, user)
    unless membership.nil?
      membership.update_attributes :acceptance_status => true
      respond_to do |format|
        format.html { redirect_to group, notice: "Now member of #{group.name}" }
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
  def remove_user
    group = Group.find(params[:group_id])
    user = User.find(params[:user_id])
    if (group_owner? group.id) && (user != current_user)
      group.users.delete user
      respond_to do |format|
        format.html { redirect_to group, notice: 'Removed user successfully.' }
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
  def unsubscribe
    if (!group_owner? params[:id]) && (group_member? params[:id])
      set_group
      current_user.groups.delete(@group)
      respond_to do |format|
        format.html { redirect_to group_path(@group), notice: 'Unsubscribed successfully.' }
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
  def cancel_invite
    if (group_owner? params[:group_id])
      membership = Membership.find_by_user_id_and_group_id_and_acceptance_status(params[:user_id], params[:group_id], false)
      Membership.delete membership unless membership.nil?
      respond_to do |format|
        format.html { redirect_to group_path(Group.find(params[:group_id])), notice: 'Cancelled invitation successfully.' }
        format.json { head :no_content }
      end
    else
      respond_to do |format|
        format.html { redirect_to group_path(Group.find(params[:group_id])), alert: "Nothing to cancel. The user has not at all been invited."}
        format.json { render json: "Only invited members may be cancelled.", status: :method_not_allowed }
      end
    end
  end
  
  private

  def set_invites
    @invites = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , current_user, false)    
  end
  
  def set_group
    begin
      @group = Group.find(params[:id])
    rescue ActiveRecord::RecordNotFound
      #render :file => "public/404.html", :status => :unauthorized, :layout => false
      redirect_to groups_path, alert: "Specified group, #{params[:id]}, does not exist"
    end
  end

  def group_member?(group_id)
    return !Membership.where("user_id = ? and group_id = ? and acceptance_status = ?", current_user, group_id, true).empty?
  end

  def group_owner?(group_id)
    return !Group.where("user_id = ? and id = ?", current_user, group_id).empty?
  end

  # Never trust parameters from the scary internet, only allow the white list through.
  def group_params
    params.require(:group).permit(:name)
  end

  def user_receiver_params
    params.permit(:users => [])
  end

  def share_params
    params.permit(:bookmark_id)
  end

end