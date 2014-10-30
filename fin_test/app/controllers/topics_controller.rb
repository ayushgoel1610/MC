class TopicsController < ApplicationController
	skip_before_action :require_token,only:[:topicList,:new,:create]
	protect_from_forgery with: :null_session, if: Proc.new { |c| c.request.format == 'application/json' }
	def show
		@topic=Topic.find(params[:id])
		respond_to do |format|
			format.json {render :json =>@topic}
			format.html {render :action => 'show'}
		end
	end
	def new
		@topic=Topic.new
	end
	def create
		@topic=Topic.new(topic_params)
		@topic.health=0
		respond_to do |format|
      		if @topic.save
      			puts "hello"
        		format.json { render json: @topic, status: :created, location: @topic }
        		format.html {render :action => 'show'}
      		else
        		format.json { render json: @topic.errors, status: :unprocessable_entity }
        		format.html {render :action => 'show'}
        	end
      	end
	end
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
	def topicList
		@topics=Topic.order(health: :desc).limit(10).offset(params[:offset])
		@topicList={
			list: @topics,
			size: @topics.size
		}.to_json
		render :json => @topicList
	end
	private
		def topic_params
			params.require(:topic).permit(:name,:category)
		end
end
