class CreateChats < ActiveRecord::Migration
  def change
    create_table :chats do |t|
    	t.integer	:userid_1
    	t.integer	:userid_2
    	t.integer	:topic_id
    	t.integer	:reputation_1
    	t.integer	:reputation_2

      t.timestamps
    end
  end
end
