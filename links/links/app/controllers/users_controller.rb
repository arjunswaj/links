class UsersController < ApplicationController
  before_action :authenticate_user!
  layout 'base'
  
  def show
    @user = User.find(params[:id])
  end
end
