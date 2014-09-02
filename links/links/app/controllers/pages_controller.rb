# Controller fro static pages
class PagesController < ApplicationController
	# Authenticate user only for tools
	before_action :authenticate_user!, only: [:tools]
	layout 'base'
	
	# GET pages/tools
  def tools
  end

  # GET pages/about
  def about
  end

  # GET pages/contact
  def contact
  end
end
