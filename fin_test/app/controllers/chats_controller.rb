class ChatsController < ApplicationController
	skip_before_action :require_token
	def create
		@chat=Chat.new(chat_params)
		respond_to do |format|
      		if @chat.save
        		format.json { render json: @chat, status: :created, location: @chat }
      		else
        		format.json { render json: @chat.errors, status: :unprocessable_entity }
        	end
      	end
	end
	def pair_request
		@topic=Topic.find(params[:topic_id])
		@topic.chatqueues.create(user_id1: params[:user_id],req_count: 0)
		response={
			status: "Wait for pairing"
		}.to_json

		render :json => response

	end
	def ret_pair
		@topic=Topic.find(params[:topic_id])
		@userlist=@topic.chatqueues
		@targetpair=@topic.chatqueues.find_by_user_id1(params[:user_id])
		@targetpair.req_count+=1
		@targetpair.save
		if @targetpair.user_id2.nil?
			for element in @topic.chatqueues
				if(element.user_id1!=params[:user_id])
					response={
						user_id: element.user_id1
					}.to_json
					element.user_id2=params[:user_id]
					element.save
					@targetpair.delete
					render :json => response
					return
				end
			end
			response={
				status: "No users available"
			}.to_json
			if(@targetpair.req_count==3)
				@targetpair.delete
				response={
					status: "No users available!Request expired."
				}
			end
			render :json => response
		else
			response={
				user_id: @targetpair.user_id2
			}.to_json
			@targetpair.delete
			render :json => response
			return
		end

		
	end
	def end_chat
		@chat=Chat.find(params[:id])
		@user1=@chat.userid_1
		@user2=@chat.userid_2
		@topic=Topic.find(@chat.topic_id)
		if(params[:userid]==@chat.userid_1)
			@chat.reputation_2=params[:reputation]
			@user2.reputation += params[:reputation]
			UsersController.new.update_recents(@chat.userid_2,@chat.userid_1,params[:reputation],@chat.topic_id)
			UsersController.new.update_userTopics(@chat.userid_2,@chat.topic_id,params[:reputation])
		elsif (params[:userid]==@chat.userid_2)
			@chat.reputation_1=params[:reputation]
			@user1.reputation+=params[:reputation]
			UsersController.new.update_recents(@chat.userid_2,@chat.userid_1,params[:reputation],@chat.topic_id)
			UsersController.new.update_userTopics(@chat.userid_1,@chat.topic_id,params[:reputation])
		end
		topic_total=@topic.health*@topic.user_count
		TopicsController.new.incr_user_count(@chat.topic_id)
		@topic.health=(topic_total+params[:reputation])/@topic.user_count
		@topic.save



	end
	private
		def chat_params
			params.require(:chat).permit(:userid_1,:userid_2,:topic_id)
		end
end
