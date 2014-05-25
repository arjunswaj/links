json.array!(@urls) do |url|
  json.extract! url, :id, :url
  json.url url_url(url, format: :json)
end
