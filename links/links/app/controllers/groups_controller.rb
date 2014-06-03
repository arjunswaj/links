class GroupsController < ApplicationController
  before_action :authenticate_user!
  # GET /groups
  # GET /groups.json
  def index
#    @groups = Group.joins(:users).where("groups_users.user_id = ? ", current_user)
    @owner_of_groups = Group.where("user_id = ?", current_user)
    @member_in_groups = Group.joins(:users).where("groups_users.user_id = ? and groups.user_id != ?" , current_user, current_user)
  end

  # GET /groups/1
  # GET /groups/1.json
  def show
    if group_member?
      set_group
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
  def edit
    if group_owner?
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
    if group_owner?
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
    if group_owner?
      set_group
      @group.destroy
    end
    respond_to do |format|
      format.html { redirect_to groups_url }
      format.json { head :no_content }
    end
  end

  def add_users
    if group_owner?
      set_group
      user_receiver_params.each do |email|
        @group.users << User.find_by_email(email)
        redirect_to @group
      end
    else
      respond_to do |format|
        format.html { redirect_to groups_path }
        format.json { head :no_content}
      end
    end
  end

  def unsubscribe
    if !group_owner? && group_member?
      set_group
      current_user.groups.delete(@group)
      #current_user.update(groups: groups)
    end
    respond_to do |format|
      format.html { redirect_to groups_url }
      format.json { head :no_content }
    end
  end

  private

  def set_group
    @group = Group.find(params[:id])
  end

  def group_member?
    return !Group.joins(:users).where("groups_users.user_id = ? and groups_users.group_id = ?", current_user, params[:id]).empty?
  end

  def group_owner?
    return !Group.where("user_id = ? and id = ?", current_user, params[:id]).empty?
  end

  # Never trust parameters from the scary internet, only allow the white list through.
  def group_params
    params.require(:group).permit(:name)
  end

  def user_receiver_params
    params.permit(:users => [])
  end

end
