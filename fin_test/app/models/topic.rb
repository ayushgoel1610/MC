class Topic < ActiveRecord::Base
	has_many :chatqueues

	def self.search(query)
		where("name like ?","%#{query}%")
	end
	def self.incrhealth
		@topics=Topic.all
		@topics.each do |topic|
			
				topic.health+=1
				topic.save

		end
	end
end
