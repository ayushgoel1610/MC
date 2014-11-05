class Addchattochatqueue < ActiveRecord::Migration
  def change
  	add_column :chatqueues,:chat,:integer
  end
end
