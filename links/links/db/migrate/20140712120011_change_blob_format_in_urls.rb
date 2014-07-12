class ChangeBlobFormatInUrls < ActiveRecord::Migration
  def change
  	change_column :urls, :icon, :binary, :limit => 10.megabyte
  end
end
