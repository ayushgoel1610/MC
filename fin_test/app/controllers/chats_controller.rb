class ChatsController < ApplicationController
	def create
		@chat=Chat.new(chat_params)
		respond_to do |format|
      		if @chat.save
        		format.json { render json: @chat, status: :created, location: @chat }
      		else
        		format.json { render json: @chat.errors, status: :unprocessable_entity }
      	end
	end
	def end_chat
		@chat=Chat.find(params[:id])
		@user1=@chat.userid_1
		@user2=@chat.userid_2
		if(params[:userid]==@chat.userid_1)
			@chat.reputation_2=params[:reputation]
			UsersController.new.update_recents(@userid_2,@userid_1,params[:reputation],@chat.topic_id)
		else if (params[:userid]==@chat.userid_2)
			@chat.reputation_1=params[:reputation]
			UsersController.new.update_recents(@userid_2,@userid_1,params[:reputation],@chat.topic_id)
		end
		TopicsController.new.incr_user_count(@chat.topic_id)



	end
	private
		def chat_params
			params.require(:chat).permit(:userid_1,:userid_2,:topic_id)
		end
end
