class UserIdinrecents < ActiveRecord::Migration
  def change
  	add_column :recents,:user_id,:integer
  end
end
