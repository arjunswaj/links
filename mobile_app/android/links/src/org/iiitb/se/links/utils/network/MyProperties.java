package org.iiitb.se.links.utils.network;

public class MyProperties {
  private static MyProperties mInstance= null;

  public String groupId;
  public String BASE_URL;
  
  protected MyProperties(){}

  public static synchronized MyProperties getInstance(){
    if(null == mInstance){
      mInstance = new MyProperties();
    }
    return mInstance;
  }
}
