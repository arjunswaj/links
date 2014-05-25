class Bookmark < ActiveRecord::Base
	belongs_to :user
	belongs_to :url  #TODO: What about autosave?
	accepts_nested_attributes_for :url
end
