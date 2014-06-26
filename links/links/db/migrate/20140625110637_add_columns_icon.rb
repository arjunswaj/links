class AddColumnsIcon < ActiveRecord::Migration
  def change
		add_column :urls, :icon, :blob
  end
end
