class AddreqCounttochatqueue < ActiveRecord::Migration
  def change
  	add_column :chatqueues,:req_count,:integer
  end
end
