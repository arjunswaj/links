class FixColumnName < ActiveRecord::Migration
  def change
    rename_column :groups, :user_id, :owner_id
  end
end
