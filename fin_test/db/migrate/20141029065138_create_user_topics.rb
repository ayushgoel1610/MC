class CreateUserTopics < ActiveRecord::Migration
  def change
    create_table :user_topics do |t|
    	t.integer :topic_id
    	t.integer :num_chats
    	t.integer :avg_reputation

      t.timestamps
    end
  end
end
