class CreateUserGroupJoinTable < ActiveRecord::Migration
  def change
    create_table 'groups_users', :id => false do |t|
    t.column :group_id, :integer
    t.column :user_id, :integer
  end
  end
end
