class BookmarksController < ApplicationController
  before_action  :authenticate_user!, only: [:show, :edit, :update, :destroy, :index, :timeline] #TODO: Can I have a set_bookmark here??

  layout "links"

  def timeline
    index
    new
  end
  
  def saveurl
    url = Url.find_by_url(link_params[:url])
    if url.nil?
      url = Url.new({:url => link_params[:url]})
      if !url.save
        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }              
      end
    end
    @bookmark = Bookmark.new({:url => url, :user => current_user})
    respond_to do |format|
      format.html { render action: 'bookmark_form' }
      format.js
    end
  end

  def savebookmark
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    url = Url.find_by_id(timeline_bookmark_params[:url_id])
    if url.nil?
       format.html { redirect_to 'new', notice: 'Trouble saving the url.' }    
    end
    @bookmark = Bookmark.new({:title => timeline_bookmark_params[:title], :description => timeline_bookmark_params[:description], :url => url, :user => current_user})

    tags = timeline_bookmark_params[:tags].split(",")
    tags.each do |tag|
        if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
          @tag = Tag.new
          @tag.tagname = tag.strip.gsub(' ','-').downcase 
          @bookmark.tags << @tag
        else
          @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
        end
    end
    #@bookmark = Bookmark.new(bookmark_params) #TODO: Explore this.. Above is Ugly
    respond_to do |format|
      if @bookmark.save
        format.html { redirect_to @bookmark, notice: 'Bookmark was successfully created.' }
        format.json { render action: 'show', status: :created, location: @bookmark }
        format.js
      else
        format.html { render action: 'new' }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end
  # GET /bookmarks
  # GET /bookmarks.json
  def index
		@bookmark_plugins = PLUGIN_CONFIG['bookmark']
    @bookmarks = current_user.bookmarks.order('created_at DESC')
  end

  # GET /bookmarks/1
  # GET /bookmarks/1.json
  def show
		set_bookmark
  end

  # GET /bookmarks/new
  def new
    @bookmark = Bookmark.new
		@bookmark.build_url
  end

  # GET /bookmarks/1/edit
  def edit
		set_bookmark		
  end

  # POST /bookmarks
  # POST /bookmarks.json
  def create
	  url = Url.find_by_url(bookmark_params[:url_attributes][:url])
		if url.nil?
			url = Url.new({:url => bookmark_params[:url_attributes][:url]})
			if !url.save
        format.html { redirect_to 'new', notice: 'Trouble saving the url.' } #TODO: What is happening here?
			end
		end
    
    @bookmark = Bookmark.new({:title => bookmark_params[:title], :description => bookmark_params[:description], :url => url, :user => current_user})

    tags = bookmark_params[:tags].split(",")
    tags.each do |tag|
        if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
          @tag = Tag.new
          @tag.tagname = tag.strip.gsub(' ','-').downcase 
          @bookmark.tags << @tag
        else
          @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
        end
    end
    #@bookmark = Bookmark.new(bookmark_params) #TODO: Explore this.. Above is Ugly
    respond_to do |format|
      if @bookmark.save
        format.html { redirect_to @bookmark, notice: 'Bookmark was successfully created.' }
        format.json { render action: 'show', status: :created, location: @bookmark }
      else
        format.html { render action: 'new' }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /bookmarks/1
  # PATCH/PUT /bookmarks/1.json
  def update
		set_bookmark
    respond_to do |format|
	    new_url = Url.find_by_url(bookmark_params[:url_attributes][:url])
			if new_url == nil
					new_url = Url.new(bookmark_params[:url_attributes])
					new_url.save
			end

			#TODO:Can I make this more ugly?
			if @bookmark.update_attributes({:title => bookmark_params[:title], :description => bookmark_params[:description], :url => new_url})
      #if @bookmark.update(bookmark_params)
        format.html { redirect_to @bookmark, notice: 'Bookmark was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: 'edit' }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /bookmarks/1
  # DELETE /bookmarks/1.json
  def destroy
		set_bookmark
    @bookmark.destroy
    respond_to do |format|
      format.html { redirect_to bookmarks_url }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_bookmark
      @bookmark = Bookmark.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def bookmark_params
      params.require(:bookmark).permit(:title, :description, :url_attributes => :url)
    end

    def link_params
      params.permit(:url)
    end

    def timeline_bookmark_params
      params.permit(:url, :url_id, :title, :description, :tags)
    end
end
