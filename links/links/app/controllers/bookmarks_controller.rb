class BookmarksController < ApplicationController
  before_action  :authenticate_user!, only: [:show, :edit, :update, :destroy, :index, :timeline]
  before_action  :set_bookmark, only: [:show, :edit, :update, :destroy]

  layout "links"

  def timeline
    index
    new
  end

  def editbookmark
    set_bookmark 
    respond_to do |format|      
      format.js
    end
  end

  def saveurl
    url = Url.find_by_url(link_params[:url])
    if url.nil?
      url = Url.new({:url => link_params[:url]})
      if !url.save
        render :status => 404
      end
    end
    @bookmark = Bookmark.new({:url => url, :user => current_user})
    respond_to do |format|
      format.html { render action: 'bookmark_form' }
      format.js
    end
  end

  #TODO: Do a check whether the URL and Bookmark actually belongs to user or not
  def savebookmark
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    url = Url.find_by_url(timeline_bookmark_params[:url])
    if url.nil?
      url = Url.new({:url => timeline_bookmark_params[:url]})
      if !url.save
        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }
      end
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

  def updatebookmark
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    url = Url.find_by_url(timeline_bookmark_params[:url])
    if url.nil?
      url = Url.new({:url => timeline_bookmark_params[:url]})
      if !url.save
        render :status => 404
      end
    end
    @bookmark = Bookmark.find_by_id(timeline_bookmark_params[:bookmark_id])
    if @bookmark.nil?
      render :status => 404
    end
    @bookmark.title = timeline_bookmark_params[:title]
    @bookmark.description = timeline_bookmark_params[:description]
    @bookmark.url = url
    @bookmark.user = current_user

    @bookmark.tags.clear

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
    @bookmarks = current_user.bookmarks.order('updated_at DESC')
  end

  # GET /bookmarks/1
  # GET /bookmarks/1.json
  def show
  end

  # GET /bookmarks/new
  def new
    @bookmark = Bookmark.new
    @bookmark.build_url
  end

  # GET /bookmarks/1/edit
  def edit    
    respond_to do |format|
      format.html { render action: 'bookmark_form' }
      format.js
    end
  end

  # POST /bookmarks
  # POST /bookmarks.json
  def create
    @bookmark = Bookmark.new(bookmark_params)

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
    respond_to do |format|
      new_url = Url.find_by_url(bookmark_params[:url_attributes][:url])
      if new_url == nil
        update_bookmark_params = bookmark_params
      else
        update_bookmark_params = bookmark_params.except :url_attributes
        update_bookmark_params.merge! :url_attributes => {:id => new_url.id, :url => new_url.url}
      end

      if @bookmark.update update_bookmark_params
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
    @bookmark.destroy
    respond_to do |format|
      format.html { redirect_to "/timeline" }
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
    params.permit(:url, :url_id, :title, :description, :tags, :bookmark_id)
  end
end
