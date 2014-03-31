class LinksController < ApplicationController
	before_filter :authenticate_user, :only => [:show, :index, :new, :create]	
  def show  	
  	@link = Link.find(params[:id])
  end

  def index
  	@links = @current_user.links
  end

  def new
    @link = Link.new
  end

  def create
    @link = Link.new(link_params)
    @current_user.links << @link
    respond_to do |format|
      if @link.save
        format.html { redirect_to :url=>{:action=>'index'}, notice: 'User was successfully created.' }        
      else
        format.html { render action: 'new' }
      end
    end
  end

  def link_params
      params.require(:link).permit(:url, :title, :description)
    end
end
