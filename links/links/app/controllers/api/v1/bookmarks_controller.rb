module Api 
	module V1	
		require 'nokogiri'
		require 'open-uri'
		require 'timeout'
	
		class BookmarksController < ApplicationController
			include BookmarksHelper
			doorkeeper_for :all
			skip_before_action :verify_authenticity_token
			respond_to :json
			def timeline
				bookmarks_loader(Time.now, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter				
			end

			def loadmore
				time = Time.at(params[:time].to_i).to_datetime
				bookmarks_loader(time, doorkeeper_token.resource_owner_id) 
				bookmarks_formatter
			end

			def index
				bookmarks = Bookmark.where("user_id == ?", doorkeeper_token.resource_owner_id)
				formatted_bookmarks = []
				bookmarks.each do  |bookmark|
				  formatted_tags = []
				  bookmark.tags.each do |tag|
				    formatted_tags << tag.tagname
				  end			  
				  formatted_bookmarks << {:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :tags => formatted_tags}
				end
				respond_with formatted_bookmarks
			end

			def savebookmark
				url_str = save_bookmark_params[:url]
				annotations = get_annotations(url_str)
				url = Url.find_by_url(url_str)
			    if url.nil?
			      url = Url.new({:url => save_bookmark_params[:url], :icon => annotations[:icon]})
			      if !url.save
			        render :status => 404
			      end
			    else
			        Url.update(url.id, :icon => annotations[:icon]) if url.icon.nil? && annotations[:icon] != ''
			    end		    
			    @bookmark = Bookmark.new({:title => save_bookmark_params[:title], :description => save_bookmark_params[:description], :url => url, :user_id => doorkeeper_token.resource_owner_id})			    

			    tags = save_bookmark_params[:tags].split(",")
			    tags.each do |tag|
			      if Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).size == 0
			        @tag = Tag.new
			        @tag.tagname = tag.strip.gsub(' ','-').downcase
			      @bookmark.tags << @tag
			      else
			        @bookmark.tags << Tag.where(:tagname => tag.strip.gsub(' ', '-').downcase).first
			      end
			    end unless tags.nil?			    
			    
			    if @bookmark.save
			    	respond_to do |format|
  						format.json{ render :json => strip_bookmark_to_json(@bookmark).to_json }
					end			        
			    else
			       render :status => 404
			    end			    
			end		

			def deletebookmark
				bookmark = Bookmark.find_by_id(params[:id])
				if bookmark.user_id == doorkeeper_token.resource_owner_id
					bookmark.destroy
					head :ok
				else
					render :status => 504
				end
			end	

			def share_bookmark_to_groups
			    bookmark_to_share = Bookmark.find(share_to_group_params['bookmark_id'])
			    group_ids = share_to_group_params['group_ids']
			    puts group_ids.to_s
			    @bookmarks = Array.new
			    group_ids.each do |group_id|
			      bookmark_to_save = Bookmark.new({:title => bookmark_to_share.title,
			        :description => bookmark_to_share.description,
			        :url => bookmark_to_share.url,
			        :user_id => doorkeeper_token.resource_owner_id,
			        :group_id => group_id,
			        :tags => bookmark_to_share.tags})
			      if !bookmark_to_save.save
			        format.html { redirect_to 'new', notice: 'Trouble saving the url.' }
			      end
			      @bookmarks << bookmark_to_save
			    end	
			    head :ok
			end

			private

			def bookmarks_formatter
				formatted_bookmarks = []
				@bookmarks.each do  |bookmark|
					formatted_bookmarks << strip_bookmark_to_json(bookmark)
				end
				respond_with formatted_bookmarks
			end

			def strip_bookmark_to_json(bookmark)
				formatted_tags = []
				bookmark.tags.each do |tag|
				   formatted_tags << tag.tagname
				end			  
				{:id => bookmark.id, :url => bookmark.url.url, :title => bookmark.title, :description => bookmark.description, :updated_at => bookmark.updated_at.to_i, :tags => formatted_tags}
				
			end
			def save_bookmark_params
			    params.permit(:url, :title, :description, :tags)
			end

			def share_to_group_params
			    params.permit(:bookmark_id, :group_ids => [])
			end

			# Extract annotations from url
		    def get_annotations(url)
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
		        title = doc.at_css('title').text if doc.at_css('title').text
		        doc.css('meta').each do |meta|
		          desc = meta['content'] if meta['name'] && (meta['name'].match 'description')
		          keywords = meta['content'].split(",") if meta['name'] && (meta['name'].match 'keywords')

		          if meta['property'] && (meta['property'].match 'og:image')
		            image_url = meta['content']
		            image_url = meta['content'].insert(0, 'http:') if meta['content'].match('^http').nil?
		            open(image_url) do |f|
		              icon = f.read
		            end
		          end
		        end
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
		end
	end
end
