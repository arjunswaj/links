class CreateMemberships < ActiveRecord::Migration
  def change
    create_table :memberships do |t|
      t.boolean :acceptance_status, :default => false

      t.timestamps
    end
  end
end
