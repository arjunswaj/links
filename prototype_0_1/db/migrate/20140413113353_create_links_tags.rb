class CreateLinksTags < ActiveRecord::Migration
  def change
    create_table :links_tags, :id => false do |t|
		t.references :link
		t.references :tag
    end
  end

  def self.down
	  drop_table :links_tags
  end
end
