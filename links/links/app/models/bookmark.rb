class Bookmark < ActiveRecord::Base
	belongs_to :user
	belongs_to :group
	belongs_to :url
	accepts_nested_attributes_for :url
	has_and_belongs_to_many :tags
end
