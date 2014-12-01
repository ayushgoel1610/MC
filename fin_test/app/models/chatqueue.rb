class Chatqueue < ActiveRecord::Base
	belongs_to :topic
	validates_uniqueness_of :user_id1
end
