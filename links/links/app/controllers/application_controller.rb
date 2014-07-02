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
  def get_annotations(url)
    require 'nokogiri'
    require 'open-uri'
    require 'timeout'
    # TODO: handle exceptions from openuri(network related)
    title = ''
    desc = ''
    keywords = []
    icon = nil

    begin
        doc = nil
      Timeout::timeout(50) {
        doc = Nokogiri::HTML(open(process_uri(url)))
      }
      doc.css('meta').each do |meta|
        title = meta['content'] if meta['name'] && (meta['name'].match 'og:title')
        desc = meta['content'] if meta['name'] && ((meta['name'].match 'description') || (meta['name'].match 'og:description'))
        keywords = meta['content'].split(",") if meta['name'] && (meta['name'].match 'keywords')

        if meta['property'] && (meta['property'].match 'og:image')
          image_url = meta['content']
          image_url = meta['content'].insert(0, 'http:') if meta['content'].match('^http').nil?
          open(image_url) do |f|
            icon = f.read
          end
        end
      end
      title = doc.at_css('title').text if title == '' && doc.at_css('title').text
    rescue Timeout::Error => ex
      logger.debug ex
      flash[:notice] = "Taking too long to retrieve annotations... :-(. Fill them yourself"
    rescue OpenURI::HTTPError => ex
      logger.debug ex
      flash[:notice] = ex.to_s
    ensure
      return {:title => title, :desc => desc, :keywords => keywords, :icon => icon}
    end
  end
  private

  # If your model is called User
  def after_sign_in_path_for(resource)
    if request.env['omniauth.origin']
      request.env['omniauth.origin']
    else
      session["user_return_to"] || root_path
    end    
  end

end
