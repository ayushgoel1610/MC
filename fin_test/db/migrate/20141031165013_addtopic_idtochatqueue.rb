class AddtopicIdtochatqueue < ActiveRecord::Migration
  def change
  	add_column :chatqueues,:topic_id,:integer
  end
end
