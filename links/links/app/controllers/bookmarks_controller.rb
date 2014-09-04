require 'nokogiri'
require 'open-uri'
require 'timeout'

# Controller for handling all operations related to Bookmarks
class BookmarksController < ApplicationController
  include BookmarksHelper
  before_action  :authenticate_user!, only: [:show, :edit, :update, :destroy, :index, :timeline, :bookmarklet]
  before_action  :set_bookmark, only: [:show, :edit, :update, :destroy]
  layout 'base'
  skip_before_action :verify_authenticity_token, only: [:bookmarklet] # TODO: http://stackoverflow.com/a/3364673
  
  # Initializes the bookmarks for the timeline page
  def timeline
    index
    new
  end

  # Handles the editing of bookmarks
  # This is an async call
  def editbookmark
    set_bookmark
    respond_to do |format|
      format.js
    end
  end

  # Shows the image
  def show_image
    set_bookmark
    send_data @bookmark.url.icon, :type => 'image',:disposition => 'inline'
  end
  
  # Persists the URL as follows:
  # * Saves the URL if not present
  # * Adds the http:// if not present
  # * Creates the bookmark object
  # * Finds the Tags, if not present, will create the object
  # * If Group id is set, will associate the bookmark with the Group
  # Handles Async requsts
  def saveurl
    url_str = link_params[:url]
    url_str.insert(0, 'http://') if url_str.match('^http').nil?
    
    annotations = get_annotations(url_str)
    
    url = Url.find_by_url(url_str)
    if url.nil?
      url = Url.new({:url => link_params[:url], :icon => annotations[:icon]})
      if !url.save
        render :status => 404
      end
    else
        Url.update(url.id, :icon => annotations[:icon]) if url.icon.nil? && annotations[:icon] != ''
    end

    @bookmark = Bookmark.new({:url => url, :title => annotations[:title], :description => annotations[:desc], :user => current_user})

    @share_with_group = Group.find(params[:id]) if params[:id]

    annotations[:keywords].each do |tag|
      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
        @tag = Tag.new
        @tag.tagname = tag.strip.gsub(' ','-').downcase
      @bookmark.tags << @tag
      else
        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
      end
    end

    respond_to do |format|
      format.html { render action: 'bookmark_form' }
      format.js
    end
  end

  # Shares Bookmarks to Groups as follows:
  # * Copies the bookmark
  # * Save the copy to a group
  def share_bookmark_to_groups
    bookmark_to_share = Bookmark.find(share_to_group_params['bookmark_id'])
    group_ids = share_to_group_params['group_ids']

    @bookmarks = Array.new
    group_ids.each do |group_id|
      bookmark_to_save = Bookmark.new({:title => bookmark_to_share.title,
        :description => bookmark_to_share.description,
        :url => bookmark_to_share.url,
        :user => current_user,
        :group_id => group_id,
        :tags => bookmark_to_share.tags})
      if !bookmark_to_save.save
        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }
      end
      @bookmarks << bookmark_to_save
    end
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
  end

  # TODO: Do a check whether the URL and Bookmark actually belongs to user or not
  # Sets the plugin info
  # Persists the Bookmark as follows:
  # * Saves the URL if not present
  # * Saves the bookmark object
  # * Finds the Tags, if not present, will save the object
  # * If Group id is set, will associate the bookmark with the Group and saves it
  # Handles Async requsts
  def savebookmark
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    url = Url.find_by_url(timeline_bookmark_params[:url])
    if url.nil?
      url = Url.new({:url => timeline_bookmark_params[:url]})
      if !url.save
        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }
      end
    end
    if params[:id].nil?
      @bookmark = Bookmark.new({:title => timeline_bookmark_params[:title], :description => timeline_bookmark_params[:description], :url => url, :user => current_user})
    else
      @bookmark = Bookmark.new({:title => timeline_bookmark_params[:title], :description => timeline_bookmark_params[:description], :url => url, :user => current_user, :group => Group.find(params[:id])})
    end

    tags = timeline_bookmark_params[:tags]
    tags.each do |tag|
      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
        @tag = Tag.new
        @tag.tagname = tag.strip.gsub(' ','-').downcase
      @bookmark.tags << @tag
      else
        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
      end
    end unless tags.nil?
    #@bookmark = Bookmark.new(bookmark_params) #TODO: Explore this.. Above is Ugly

    respond_to do |format|
      if @bookmark.save
        format.html { redirect_to timeline_path, notice: 'Bookmark was successfully created.' }
        format.json { render action: 'show', status: :created, location: @bookmark }
        format.js
      else
        format.html { render action: 'new' }
        format.json { render json: @bookmark.errors, status: :unprocessable_entity }
      end
    end
  end

  # Sets the plugin info
  # Updates the Bookmark as follows:
  # * Saves the new URL if not present
  # * Updates the bookmark object
  # * Finds the Tags, if not present, will save/update the object
  # * If Group id is set, will associate the bookmark with the Group and updates it
  # Handles Async requsts
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

    tags = timeline_bookmark_params[:tags]
    tags.each do |tag|
      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
        @tag = Tag.new
        @tag.tagname = tag.strip.gsub(' ','-').downcase
      @bookmark.tags << @tag
      else
        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
      end
    end unless tags.nil?
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
  # Loads the bookmarks
  # This is paginated
  def index
    bookmarks_loader(Time.now, current_user.id)    
    bookmark = @bookmarks.first
    if bookmark
      session[:first_link_time] = bookmark.updated_at    
    end    
    bookmark = @bookmarks.last
    if bookmark
      session[:last_link_time] = bookmark.updated_at
    end    
  end

  # Loads more bookmarks
  # This is paginated
  def loadmore
    bookmarks_loader(session[:last_link_time], current_user.id)
    bookmark = @bookmarks.last
    if bookmark
      session[:last_link_time] = bookmark.updated_at    
    end    
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
  
  # This is used to save bookmarks from the bookmarklet
  def bookmarklet
    url_str = params[:url]
    url_str.insert(0, 'http://') if url_str.match('^http').nil?
    
    url = Url.find_by_url(url_str)
    if url.nil?
      url = Url.new({:url => params[:url], :icon => nil})
      if !url.save
        render :status => 404
      end
    end

    @bookmark = Bookmark.new({:url => url, :title => params[:title], :description => params[:description], :user => current_user})
 
    params[:tags].split(',').each do |tag|
      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
        @tag = Tag.new
        @tag.tagname = tag.strip.gsub(' ','-').downcase
        @bookmark.tags << @tag
      else
        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
      end
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

  # Util to get link params
  def link_params
    params.permit(:url)
  end

  # Util to get bookmark params in timeline
  def timeline_bookmark_params
    params.permit(:url, :url_id, :title, :description, :bookmark_id, :tags => [])
  end

  # Util to get params when shared to groups
  def share_to_group_params
    params.permit(:bookmark_id, :group_ids => [])
  end

end
