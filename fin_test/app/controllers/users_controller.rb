class UsersController < ApplicationController
	skip_before_filter  :verify_authenticity_token
	protect_from_forgery with: :null_session, if: Proc.new { |c| c.request.format == 'application/json' }
    def create
    	@user=User.new(user_params)
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
    private
    	def user_params
    		params.require(:user).permit(:name,:email,:password,:password_confirmation)
    	end
end
