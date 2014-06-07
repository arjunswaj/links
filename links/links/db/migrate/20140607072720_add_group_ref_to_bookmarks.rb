class AddGroupRefToBookmarks < ActiveRecord::Migration
  def change
    add_reference :bookmarks, :group, index: true
  end
end
