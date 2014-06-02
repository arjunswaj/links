require 'api_constraints'

Links::Application.routes.draw do

  resources :groups

  get "searches/index" => "searches#index", :as => 'search_index'
  post "searches/search_user" => "searches#search_user", :as => 'search_user'

  #dummy receiver
  post "searches/receiver" => "searches#receiver", :as => 'receiver'

  use_doorkeeper

	namespace :api,defaults: {format: 'json'} do
			scope module: :v1, constraints: ApiConstraints.new(version: 1, default: true) do
					resources :bookmarks
			end
	end  

  #resources :urls

  resources :bookmarks
  get "/timeline" => "bookmarks#timeline", :as => 'timeline'
  get ":id/editbookmark" => "bookmarks#editbookmark", :as => 'editbookmark'
  post "/saveurl" => "bookmarks#saveurl", :as => 'saveurl' 
  post "/savebookmark" => "bookmarks#savebookmark", :as => 'savebookmark'
  post "/updatebookmark" => "bookmarks#updatebookmark", :as => 'updatebookmark'

  devise_for :users

	get 'annotations', to: 'scrapper#annotations'

  root to: 'bookmarks#timeline'

  mount Share::Engine, :at => "/share"
  mount Ad::Engine, :at => "/ad"

  # The priority is based upon order of creation: first created -> highest priority.
  # See how all your routes lay out with "rake routes".

  # You can have the root of your site routed with "root"
  # root 'welcome#index'

  # Example of regular route:
  #   get 'products/:id' => 'catalog#view'

  # Example of named route that can be invoked with purchase_url(id: product.id)
  #   get 'products/:id/purchase' => 'catalog#purchase', as: :purchase

  # Example resource route (maps HTTP verbs to controller actions automatically):
  #   resources :products

  # Example resource route with options:
  #   resources :products do
  #     member do
  #       get 'short'
  #       post 'toggle'
  #     end
  #
  #     collection do
  #       get 'sold'
  #     end
  #   end

  # Example resource route with sub-resources:
  #   resources :products do
  #     resources :comments, :sales
  #     resource :seller
  #   end

  # Example resource route with more complex sub-resources:
  #   resources :products do
  #     resources :comments
  #     resources :sales do
  #       get 'recent', on: :collection
  #     end
  #   end

  # Example resource route with concerns:
  #   concern :toggleable do
  #     post 'toggle'
  #   end
  #   resources :posts, concerns: :toggleable
  #   resources :photos, concerns: :toggleable

  # Example resource route within a namespace:
  #   namespace :admin do
  #     # Directs /admin/products/* to Admin::ProductsController
  #     # (app/controllers/admin/products_controller.rb)
  #     resources :products
  #   end
end
