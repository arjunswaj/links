Ad::Engine.routes.draw do
	get "/index" => "pages#index", :as => 'page_ads' 
end
