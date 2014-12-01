class SessionsController < ApplicationController
  skip_before_action :require_token,only:[:new,:create]
  def new
  end
  def create(email,password)
  	@user = User.authenticate(email,password)
  		if @user
        key=ApiKey.create.access_token
        puts "hello"
        puts @user.id
  			@user.token=key
        puts @user.inspect
  			@user.save
  			return @user
  		else
  			nil
  	  end
  end
  def destroy
  	
  end


end
