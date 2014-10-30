class UsersController < ApplicationController
    skip_before_action :require_token,only:[:new,:create]
	skip_before_filter  :verify_authenticity_token
	protect_from_forgery with: :null_session, if: Proc.new { |c| c.request.format == 'application/json' }
    def create
    	@user=User.new(user_params)
        @user.reputation=0
    	puts @user.name
    	puts @user.password
    	respond_to do |format|
      		if @user.save
        		format.json { render json: @user, status: :created, location: @user }
      		else
        		format.json { render json: @user.errors, status: :unprocessable_entity }
      	end
    end
    end
    def show
    	@user=User.find(params[:id])
    	respond_to do |format|
    		format.json {render :json => @user}
    	end
    end
    def update_recents(userid_1,userid_2,reputation,topic_id)
        @user=User.find(userid_1)
        @user.recents.create(topic_id: topic_id,chat_user_id: userid_2,reputation: reputation)
    end
    def update_userTopics(user_id,topic_id,reputation)
        @user=User.find(user_id)
        @topics=@user.UserTopic.all
        @topic=@topics.find_by_topic_id(topic_id)
        if(@topic)
            @topic.num_chats +=1
            @topic.avg_reputation=(total_rep+reputation)
            @topic.save
        else
            @user.userTopics.create(topic_id: topic_id,num_chats: 1,avg_reputation: reputation)
        end
    end
    private
    	def user_params
    		params.require(:user).permit(:name,:email,:password,:password_confirmation)
    	end
end
