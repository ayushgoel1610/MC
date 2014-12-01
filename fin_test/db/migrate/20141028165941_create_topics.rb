class CreateTopics < ActiveRecord::Migration
  def change
    create_table :topics do |t|
    	t.string :name
    	t.string :category
    	t.integer :user_count
    	t.integer :health

      t.timestamps
    end
  end
end
