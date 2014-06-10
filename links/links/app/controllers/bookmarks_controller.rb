require 'nokogiri'
require 'open-uri'

class BookmarksController < ApplicationController
  before_action  :authenticate_user!, only: [:show, :edit, :update, :destroy, :index, :timeline]
  before_action  :set_bookmark, only: [:show, :edit, :update, :destroy]
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

    # extract annotations from url
    # TODO: handle exceptions from openuri(network related)
    doc = Nokogiri::HTML(open(url.url))
    title = ''
    desc = ''
    title = doc.at_css("title").text if doc.at_css('title').text 
    doc.css("meta").each do |meta|
      if meta['name'] && (meta['name'].match 'description')
        desc = meta['content']
        break
      end
    end

    @bookmark = Bookmark.new({:url => url, :title => title, :description => desc, :user => current_user})
    respond_to do |format|
      format.html { render action: 'bookmark_form' }
      format.js
    end
  end

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

    tags = timeline_bookmark_params[:tags]
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

    tags = timeline_bookmark_params[:tags]
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
    bookmarks_loader(Time.now)    
    bookmark = @bookmarks.first
    session[:first_link_time] = bookmark.updated_at
    bookmark = @bookmarks.last
    session[:last_link_time] = bookmark.updated_at
  end

  def loadmore
    bookmarks_loader(session[:last_link_time])
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
    params.permit(:url, :url_id, :title, :description, :bookmark_id, :tags => [])
  end

  def share_to_group_params
    params.permit(:bookmark_id, :group_ids => [])
  end

  def bookmarks_loader(time)
    @bookmark_plugins = PLUGIN_CONFIG['bookmark']
    @bookmarks = Bookmark.eager_load(:tags, :user, :url)
      .eager_load(group: :memberships)
      .where("(users.id = :user_id AND bookmarks.group_id IS NULL) OR (bookmarks.group_id IS NOT NULL AND memberships.user_id = :user_id AND memberships.acceptance_status = :membership_status)", user_id: "#{current_user.id}", membership_status: "t")
      .where("bookmarks.updated_at < :now", now: time)
      .order('bookmarks.updated_at DESC')
      .limit(5)
  end
end
