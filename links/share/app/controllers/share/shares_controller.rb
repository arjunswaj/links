require_dependency "share/application_controller"

module Share
  class SharesController < ApplicationController
  	def share_by_email  		
  		title = URI.encode(params[:title])
  		link = URI.encode(params[:link])
  		redirect_to "mailto:?"+"Subject="+title+"&Body="+link
  	end
	
	def share_by_facebook  		
  		title = URI.encode(params[:title])
  		link = URI.encode(params[:link])
  		redirect_to "https://www.facebook.com/sharer/sharer.php?u="+link
  	end

  	def share_by_twitter  		
  		title = URI.encode(params[:title])
  		link = URI.encode(params[:link])
  		redirect_to "https://twitter.com/home?status="+link
  	end
  end
end
