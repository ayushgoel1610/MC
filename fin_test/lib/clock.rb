require 'clockwork'

require './config/boot'
require './config/environment'

module Clockwork

	handler do |job|
		if job.eql?('incrhealth')
			Topic.incrhealth
		end
	end

	every(1.minute,'incrhealth')
end