class PusherTestController < ApplicationController
	require "net/http"
	require "uri"
	def hello_world
		pop={
			hello: "sup"
		}.to_json
		data={
			name: "foo",
			channels: ["test"],
			data: pop
		}.to_json
		uri=URI.parse("http://api.pusherapp.com/apps/75548/events")
		http = Net::HTTP.new(uri.host, uri.port)
		request = Net::HTTP::Post.new(uri.request_uri)
		request.set_form_data(data)
		request["Content-Type"] = "application/json"
		response = http.request()
		render :json => response
    # Pusher['test_channel'].trigger('hh', {
    #   message: 'hello world'
    # })


  end
end
