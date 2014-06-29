require 'api_constraints'

Links::Application.routes.draw do
  
  get "delicious/authorize" => 'delicious#authorize', :as => 'delicious_authorize'
  get "delicious/import" => 'delicious#import', :as => 'delicious_import'

  resources :groups # TODO: Clean this up.
  get 'groups/members/:id' => 'groups#members', :as => 'group_members'
  get 'groups/pending_members/:id' => 'groups#pending_members', :as => 'group_pending_members'
  get "groups_owned" => 'groups#owned_groups', :as => 'owned_groups'
  get "groups_invites" => 'groups#group_invites', :as => 'group_invites'
  post "groups/:id/invite_users" => 'groups#invite_users', :as => 'invite_users_to_group'
  put "groups/:group_id/accept_invite/:user_id" => 'groups#accept_invite', :as => 'accept_invite_to_group'
  delete "groups/:group_id/user/:user_id" => 'groups#remove_user', :as => 'remove_user_from_group'
  post "groups/:id/unsubscribe" => 'groups#unsubscribe', :as => 'unsubscribe_user_from_group'
  delete "groups/:group_id/user/:user_id/cancel" => 'groups#cancel_invite', :as => 'cancel_invite_to_user_from_group'
  
  get "searches/index" => "searches#index", :as => 'search_index'
  get "searches/searchmore" => "searches#searchmore", :as => 'search_more'
  
  post "searches/search_user" => "searches#search_user", :as => 'search_user'
  get "searches/search_bookmark" => "searches#search_bookmark", :as => 'search_bookmark'

  # For oauth
  use_doorkeeper

  # For bookmarks REST API
	namespace :api,defaults: {format: 'json'} do
			scope module: :v1, constraints: ApiConstraints.new(version: 1, default: true) do	
          post "/savebookmark"	=> "bookmarks#savebookmark", :as => 'api_savebookmark'			
          get "/timeline" => "bookmarks#timeline", :as => 'api_timeline'
          get "/loadmore/:time" => "bookmarks#loadmore", :as => 'api_loadmore'
          delete "/deletebookmark/:id"  => "bookmarks#deletebookmark", :as => 'api_deletebookmark'      
			end

      scope module: :v1, constraints: ApiConstraints.new(version: 1, default: true) do          
          get "/searches/search_bookmark/:keyword" => "searches#search_bookmark", :as => 'api_search_bookmark'
          get "/searches/searchmore/:keyword/:time" => "searches#searchmore", :as => 'api_search_more'
      end

      scope module: :v1, constraints: ApiConstraints.new(version: 1, default: true) do          
          get "/groups/index" => "groups#index", :as => 'api_groups'
          get "/groups/requests" => "groups#requests", :as => 'api_groups_requests'
          put "/groups/accept/:group_id" => 'groups#accept_invite', :as => 'api_accept_invite_to_group'
          delete "/groups/reject/:group_id" => 'groups#reject_invite', :as => 'api_reject_invite_to_group'
          delete "/groups/unsubscribe/:group_id" => 'groups#unsubscribe', :as => 'api_unsubscribe_user_from_group'       
      end
	end

  resources :bookmarks
  get "/timeline" => "bookmarks#timeline", :as => 'timeline'
  get "/loadmore" => "bookmarks#loadmore", :as => 'loadmore'
  get 'icon/:id' => 'bookmarks#show_image', :as => 'icon'
  
  get ":id/editbookmark" => "bookmarks#editbookmark", :as => 'editbookmark'
  post "/saveurl" => "bookmarks#saveurl", :as => 'saveurl'
  post "/saveurl/:id" => "bookmarks#saveurl", :as => 'saveurl_for_group_share' 
  post "/savebookmark" => "bookmarks#savebookmark", :as => 'savebookmark'
  post "/savebookmark/:id" => "bookmarks#savebookmark", :as => 'savebookmark_for_group_share'
  post "/updatebookmark" => "bookmarks#updatebookmark", :as => 'updatebookmark'
  get 'bookmarklet' => 'bookmarks#bookmarklet', :as => 'bookmarklet'


# share to groups
  get "/shareable_groups/:bookmark_id" => "groups#shareable_groups", :as => 'shareable_groups'
  post "/sharebookmark" => "bookmarks#share_bookmark_to_groups", :as => 'share_bookmark_to_groups'

  # For authentication 
  devise_for :users
  get 'users/:id' => 'users#show', :as => 'show_user'

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
