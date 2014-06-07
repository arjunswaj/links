class GroupsController < ApplicationController
  before_action :authenticate_user!, :set_invites
  
  # TODO: error messages
  # TODO: using head to build header-only responses like :bad_request, :created
  
  # GET /groups
  # GET /groups.json
  # TODO: json response
  def index
    @owner_of_groups = Group.where("user_id = ?", current_user)
    @accepted_invitations = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , current_user, true)
  end

  # GET /groups/1
  # GET /groups/1.json
  # TODO: json response
  def show
    @group_owner = nil;
    if group_member?(params[:id])
      set_group
      @accepted_members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", params[:id], true)
      @pending_members = User.joins(:groups).where("group_id = ? and acceptance_status = ?", params[:id], false)
      if group_owner? params[:id]
        @group_owner = current_user
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path }
        format.json { head :no_content}
      end
    end
  end

  # GET /groups/new
  def new
    @group = Group.new
  end

  # GET /groups/1/edit
  # TODO: json response
  def edit
    if group_owner? params[:id]
      set_group
    else
      respond_to do |format|
        format.html { redirect_to groups_path }
        format.json { head :no_content}
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

        membership = Membership.where("group_id = ? and user_id = ?", @group.id, current_user)
        membership[0].update_attributes :acceptance_status => true #TODO: Can limit be applied to the query

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
        format.html { redirect_to groups_path }
        format.json { head :no_content}
      end
    end

  end

  # DELETE /groups/1
  # DELETE /groups/1.json
  def destroy
    if group_owner? params[:id]
      set_group
    @group.destroy
    end
    respond_to do |format|
      format.html { redirect_to groups_url }
      format.json { head :no_content }
    end
  end

  # POST "groups/1/invite_users"
  # TODO: json response
  def invite_users
    if group_owner? params[:id]
      set_group
      params[:users].each do |email| # TODO: Need to use white list
        user = User.find_by_email(email)
        if !@group.users.include? user
        @group.users << user
        end
      end unless params[:users].nil?
      redirect_to @group
    else
      respond_to do |format|
        format.html { redirect_to groups_path }
        format.json { head :no_content}
      end
    end
  end

  # PUT "groups/1/add_user/1"
  # TODO: json response
  def accept_invite
       group = Group.find(params[:group_id])
      user = User.find(params[:user_id])
    membership = Membership.where("group_id = ? and user_id = ?", group, user) # TODO: use orm construct which uses limit
    unless membership.empty?
      membership[0].update_attributes :acceptance_status => true
      redirect_to group
    else
      respond_to do |format|
        format.html { redirect_to groups_path }
        format.json { head :no_content}
      end
    end
  end

  # DELETE "groups/1/users/2"
  # TODO: json response
  def remove_user
    group = Group.find(params[:group_id])
    user = User.find(params[:user_id])
    if (group_owner? group.id) && (user != current_user)
    group.users.delete user
    end
    respond_to do |format|
      format.html { redirect_to group_path(group)}
      format.json { head :no_content}
    end

  end

  # POST "groups/1/unsubscribe"
  # TODO: json response
  def unsubscribe
    if (!group_owner? params[:id]) && (group_member? params[:id])
      set_group
      current_user.groups.delete(@group)
    end
    respond_to do |format|
      format.html { redirect_to groups_url }
      format.json { head :no_content }
    end
  end

  # DELETE "groups/1/users/2"
  # TODO: json response
  def cancel_invite
    if (group_owner? params[:group_id])
      membership = Membership.where("user_id = ? and group_id = ? and acceptance_status = false")
      unless membership.empty?
        Membership.delete membership[0]
      end
    end
    respond_to do |format|
      format.html { redirect_to group_path(Group.find(params[:group_id]))}
      format.json { head :no_content }
    end
  end
  
  private

  def set_invites
    @invites = Group.joins(:users).where("memberships.user_id = ? and memberships.acceptance_status = ?" , current_user, false)
  end
  
  def set_group
    @group = Group.find(params[:id])
  end

  def group_member?(group_id)
    return !Membership.where("user_id = ? and group_id = ?", current_user, group_id).empty?
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

end