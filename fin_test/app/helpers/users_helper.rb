module UsersHelper
	def update_recents(userid_1,userid_2,reputation,topic_id)
        @user=User.find(userid_1)
        @user.recents.create(topic_id: topic_id,chat_user_id: userid_2,reputation: reputation)
    end
end
