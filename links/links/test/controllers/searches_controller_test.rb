require 'test_helper'

class SearchesControllerTest < ActionController::TestCase
  test "should get index" do
    get :index
    assert_response :success
  end

  test "should get search_user" do
    get :search_user
    assert_response :success
  end

end
