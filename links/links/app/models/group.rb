class Group < ActiveRecord::Base
  belongs_to :user #owner
  has_many :memberships
  has_many :users, :through => :memberships #members
  has_many :bookmarks
end
