class PagesController < ApplicationController
	before_action :authenticate_user!, only: [:tools]
  def tools
  end

  def about
  end

  def contact
  end
end
