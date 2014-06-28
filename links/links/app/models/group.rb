class Group < ActiveRecord::Base
  belongs_to :owner, :class_name => 'User', :foreign_key => 'owner_id' #owner
  has_many :memberships
  has_many :users, :through => :memberships #members
  has_many :bookmarks
  validates :name, presence: true
	validates :description, presence: true
end
