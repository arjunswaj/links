class CreateBookmarks < ActiveRecord::Migration
  def change
    create_table :bookmarks do |t|
      t.string :title
      t.text :description
      t.integer :user_id
      t.integer :url_id

      t.timestamps
    end
  end
end
