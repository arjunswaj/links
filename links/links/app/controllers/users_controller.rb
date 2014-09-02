# Controller for actions related to users needed beyond what devise has to offer
class UsersController < ApplicationController
  before_action :authenticate_user!
  layout 'base'
  
  # GET /users/1
  # Details of the specified user
  def show
    @user = User.find(params[:id])
  end
end
