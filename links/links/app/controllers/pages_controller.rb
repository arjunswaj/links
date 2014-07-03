class PagesController < ApplicationController
	before_action :authenticate_user!, only: [:tools]
	layout 'base'
	
  def tools
  end

  def about
  end

  def contact
  end
end
