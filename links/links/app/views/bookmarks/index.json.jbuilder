json.array!(@bookmarks) do |bookmark|
  json.extract! bookmark, :id, :title, :description, :user_id, :url_id
  json.url bookmark_url(bookmark, format: :json)
end
