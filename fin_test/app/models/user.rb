class User < ActiveRecord::Base
	before_save {self.email= email.downcase}
	validates :name,presence: true,length: {maximum: 50},uniqueness: true
	validates :email,presence: true,uniqueness: {case_sensitive: false}
	has_secure_password
	validates :password,:length => {:minimum => 6}
	validates_confirmation_of :password
	has_many :recents
	has_many :userTopics
	def self.authenticate(email,password)
		user=find_by_email(email)
		if user && user.authenticate(password)
			user
		else
			nil
		end
	end
end
