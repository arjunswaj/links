require 'test_helper'

class DeliciousControllerTest < ActionController::TestCase
  test "should get authorize" do
    get :authorize
    assert_response :success
  end

  test "should get access_token" do
    get :access_token
    assert_response :success
  end

  test "should get import" do
    get :import
    assert_response :success
  end

end
