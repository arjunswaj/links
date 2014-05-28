class CreateBookmarksTags < ActiveRecord::Migration
  def change
    create_table :bookmarks_tags do |t|
    	t.references :bookmark
		t.references :tag
    end    
  end
end
