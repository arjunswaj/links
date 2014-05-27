require 'rubygems'
require 'nokogiri'
require 'open-uri'

class ScrapperController < ApplicationController
  respond_to :json
  
  def annotations
		doc = Nokogiri::HTML(open(params[:url]))
		respond_with doc.at_css("title").text
  end

  private
    # Never trust parameters from the scary internet, only allow the white list through.
    def scrapper_params
      params.require(:url)
    end
end
