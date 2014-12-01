class UserIdinusertopics < ActiveRecord::Migration
  def change
  	add_column :user_topics,:user_id,:integer
  end
end
