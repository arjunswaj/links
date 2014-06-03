class CreateBookmarkGroupJoinTable < ActiveRecord::Migration
  def change
    create_table 'bookmarks_groups', :id => false do |t|
      t.column :bookmark_id, :integer
      t.column :group_id, :integer
    end
  end
end
