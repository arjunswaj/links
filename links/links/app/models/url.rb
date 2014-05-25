class Url < ActiveRecord::Base
	has_many :bookmarks
	validates_presence_of :url
end
