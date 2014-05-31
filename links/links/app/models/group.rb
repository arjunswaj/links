class Group < ActiveRecord::Base
  belongs_to :user #owner
  has_and_belongs_to_many :users #members
end
