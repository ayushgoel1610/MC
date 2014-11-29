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
		@userlist.each do |blah|
			puts blah.user_id1
		end
		@targetpair=@topic.chatqueues.find_by_user_id1(params[:user_id])
		puts "Bleh"+@targetpair.user_id1.to_s
		@targetpair.req_count+=1
		@targetpair.save
		if @targetpair.user_id2.nil?
			for element in @topic.chatqueues
				if(element.user_id1!=params[:user_id])
					@chat=Chat.new(userid_1: params[:user_id],userid_2: element.user_id1,topic_id: params[:topic_id])
					@chat.save
					@user=User.find(element.user_id1)
					response={
						status: "user found",
						pair_id: @user.chat_id,
						chat: @chat.id
					}.to_json
					element.user_id2=params[:user_id]
					element.chat=@chat.id
					element.save
					@targetpair.delete
					puts response
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
			@chat=Chat.find(@targetpair.chat)
			@user=User.find(@targetpair.user_id2)
			response={
				status: "user found",
				pair_id: @user.chat_id,
				chat: @chat.id
			}.to_json
			@targetpair.delete
			render :json => response
			return
		end

		
	end
	def end_chat
		@chat=Chat.find(params[:id])
		@user1=User.find(@chat.userid_1)
		@user2=User.find(@chat.userid_2)
		@topic=Topic.find(@chat.topic_id)
		if(params[:user_id]==@chat.userid_1)
			@chat.reputation_2=params[:reputation]
			@user2.reputation += params[:reputation]
			@chat.save
			@user2.save
			UsersController.new.update_recents(@chat.userid_2,@chat.userid_1,params[:reputation],@chat.topic_id)
			UsersController.new.update_userTopics(@chat.userid_2,@chat.topic_id,params[:reputation])
		elsif (params[:user_id]==@chat.userid_2)
			@chat.reputation_1=params[:reputation]
			@user1.reputation+=params[:reputation]
			@chat.save
			@user1.save
			UsersController.new.update_recents(@chat.userid_2,@chat.userid_1,params[:reputation],@chat.topic_id)
			UsersController.new.update_userTopics(@chat.userid_1,@chat.topic_id,params[:reputation])
		end
		puts @topic
		if(@topic.user_count==nil)
			@topic.user_count=0;
			@topic.save
		end
		topic_total=@topic.health*@topic.user_count
		TopicsController.new.incr_user_count(@chat.topic_id)
		@topic=Topic.find(@chat.topic_id)
		@topic.health=(topic_total+params[:reputation])/@topic.user_count
		@topic.save
		response={
			status: "chat ended"
		}.to_json
		render :json => response



	end
	private
		def chat_params
			params.require(:chat).permit(:userid_1,:userid_2,:topic_id)
		end
end
