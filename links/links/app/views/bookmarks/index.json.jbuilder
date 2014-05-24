json.array!(@bookmarks) do |bookmark|
  json.extract! bookmark, :id, :title, :description
  json.url bookmark_url(bookmark, format: :json)
end
