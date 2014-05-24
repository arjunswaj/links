Share::Engine.routes.draw do
	get '/mail/title/:title/link/:link', to: 'shares#share_by_email' , :as => 'share_by_email', :title => /.*/, :link => /.*/
	get '/facebook/title/:title/link/:link', to: 'shares#share_by_facebook' , :as => 'share_by_facebook', :title => /.*/, :link => /.*/
	get '/twitter/title/:title/link/:link', to: 'shares#share_by_twitter' , :as => 'share_by_twitter', :title => /.*/, :link => /.*/
end
