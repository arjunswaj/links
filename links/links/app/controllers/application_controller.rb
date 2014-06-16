class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception
  before_action :configure_permitted_parameters, if: :devise_controller?

  protected

  def process_uri(uri)
    require 'open-uri'
    require 'open_uri_redirections'
    open(uri, :allow_redirections => :safe) do |r|
      r.base_uri.to_s
    end
  end
  
  def configure_permitted_parameters
    devise_parameter_sanitizer.for(:sign_up) do |u|
      u.permit(:name, :email, :password, :password_confirmation)
    end
    devise_parameter_sanitizer.for(:account_update) do |u|
      u.permit(:name, :email, :password, :password_confirmation, :current_password)
    end
  end
=begin
  def configure_permitted_parameters
    devise_parameter_sanitizer.for(:sign_up) << :name
    devise_parameter_sanitizer.for(:account_update) << :name
  end 
=end 

  private

  # If your model is called User
  def after_sign_in_path_for(resource)
    session["user_return_to"] || root_path
  end

end
