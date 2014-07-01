# https://github.com/SciDevs/delicious-api/blob/master/api/oauth.md
# https://github.com/SciDevs/delicious-api/blob/master/api/posts.md

# TODO: error handling
class DeliciousController < ApplicationController
  before_action :authenticate_user!, :set_client_details

  def authorize
    redirect_to 'https://delicious.com/auth/authorize?client_id=' + @client_id.to_s + '&redirect_uri=' + delicious_import_url
  end

  def import
    request_token = request.original_url.split('code=')[1]
    logger.debug request_token # TODO: remove this and the other logger.debug statements

    access_token_url = 'https://avosapi.delicious.com/api/v1/oauth/token?client_id=' + @client_id.to_s + '&client_secret=' + @client_secret.to_s + '&grant_type=code&code=' + request_token
    response = RestClient.post access_token_url, nil
    access_token = JSON.parse(response)['access_token']
    logger.debug access_token

    response = RestClient.get 'https://delicious.com/v1/posts/all?&tag_separator=comma', :Authorization => 'Bearer ' + access_token
    logger.debug response

    xml_doc = Nokogiri::XML(response)
    xml_doc.xpath('//post').each do |post|
      url_str = Url.new({:url => post.xpath('@href').to_s, :icon => nil})
      title = post.xpath('@description').to_s
      description = post.xpath('@extended').to_s
      tags = post.xpath('@tag').to_s.split(',')
      logger.debug tags
      
      url = Url.find_by_url(post.xpath('@href').to_s)
          if url.nil?
            url = Url.new({:url => post.xpath('@href').to_s, :icon => nil})
            if !url.save
              render :status => 404
            end                       
          end 

      bookmark = Bookmark.new({:url => url, :title => title, :description => description, :user => current_user})
      tags.each do |t|
        if Tag.where(:tagname => t.strip.gsub(' ', '-').downcase).size == 0
          tag = Tag.new
          tag.tagname = t.strip.gsub(' ','-').downcase
        bookmark.tags << tag
        else
          bookmark.tags << Tag.where(:tagname => t.strip.gsub(' ', '-').downcase).first
        end
      end unless tags.nil?

      if bookmark.save
        logger.debug "Saved #{post}"
      else
        logger.debug "Failed to save #{post}"
      end
    end
    redirect_to timeline_path, alert: 'Import from \'Delicious\' completed'
  end

  private

  def set_client_details
    @client_id = 'ea87805308fe893e0e07ed475d99a635'
    @client_secret = '6f391448ed6ff168a3ddb3b737948b5c'
  end

end