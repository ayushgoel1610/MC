class ApiKey < ActiveRecord::Base
	before_create :generate_access_token
 
  validates :access_token, uniqueness: true
 
  private
    def generate_access_token
      begin
        self.access_token = SecureRandom.hex
      end while self.class.exists?(access_token: access_token)
    end
end
