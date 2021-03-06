class User < ActiveRecord::Base
  # Include default devise modules. Others available are:
  # :confirmable, :lockable, :timeoutable and :omniauthable
  devise :database_authenticatable, :registerable,
         :recoverable, :rememberable, :trackable, :validatable
         
  validates :name, presence: true

	has_many :bookmarks
	has_many :groups # owner relationship
	has_many :memberships
	has_many :groups, :through => :memberships # membership relationship
	
	mount_uploader :image, ImageUploader

end
