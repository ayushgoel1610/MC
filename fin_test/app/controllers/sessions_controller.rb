class SessionsController < ApplicationController
  skip_before_action :require_token,only:[:new,:create]
  def new
  end
  def create
  	@user = User.authenticate(params[:email],params[:password])
  	respond_to do |format|
  		if @user
  			@user.token=ApiKey.create!
  			@user.save
  			format.json{render json: @user,status: "logged in"}
  		else
  			format.json{status: "Invalid Credentials"}
  		end
  	end
  end
  def destroy
  	
  end


end
