module TopicsHelper
	def incr_user_count(id)	
		@topic=Topic.find(id)
		if @topic!=nil
			if @topic.user_count==nil
				@topic.user_count=1
				@topic.save
			else
				@topic.user_count+=1
				@topic.save
			end
		end
	end
end
