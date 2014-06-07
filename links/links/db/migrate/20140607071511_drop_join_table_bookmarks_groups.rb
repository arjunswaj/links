class DropJoinTableBookmarksGroups < ActiveRecord::Migration
  def change
  	drop_table :bookmarks_groups
  end
end
