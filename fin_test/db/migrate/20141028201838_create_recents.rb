class CreateRecents < ActiveRecord::Migration
  def change
    create_table :recents do |t|
    	t.string :reputation
    	t.integer :topic_id
    	t.integer :chat_user_id

      t.timestamps
    end
  end
end
