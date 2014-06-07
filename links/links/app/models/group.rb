class Group < ActiveRecord::Base
  belongs_to :user #owner
  has_many :memberships
  has_many :users, :through => :memberships #members
  has_and_belongs_to_many :bookmarks
end
