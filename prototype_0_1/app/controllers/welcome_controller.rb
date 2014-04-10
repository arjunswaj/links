class WelcomeController < ApplicationController
  before_filter :save_login_state, :only => [:index]
  def index
  	if params[:username_or_email]
	  	authorized_user = User.authenticate(params[:username_or_email],params[:login_password])
	  	if authorized_user
				session[:user_id] = authorized_user.id	
				redirect_to url_for(:controller => 'links', :action => 'index')
		end
	end
  end
end
