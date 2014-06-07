class AddColumnsMembership < ActiveRecord::Migration
  def change
    add_column :memberships, :user_id, :integer
    add_column :memberships, :group_id, :integer
  end
end