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
	link = link_params
	tags = link.delete("tags").split(","); # TODO: fix this
    @link = Link.new(link)
	tags.each do |tag|
			if Tag.where(:tagName => tag.strip.gsub(' ', '-').downcase).size == 0
				@tag = Tag.new
				@tag.tagName = tag.strip.gsub(' ','-').downcase 
				@link.tags << @tag
			else
				@link.tags << Tag.where(:tagName => tag.strip.gsub(' ', '-').downcase).first
			end
	end
    @current_user.links << @link
    respond_to do |format|
      if @link.save
        format.html { redirect_to :url=>{:action=>'index'}, notice: 'Link was successfully created.' }        
      else
        format.html { render action: 'new' }
      end
    end
  end

  def link_params
      params.require(:link).permit(:url, :title, :description, :tags)
    end
end
