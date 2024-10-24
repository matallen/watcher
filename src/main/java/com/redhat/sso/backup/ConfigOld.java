package com.redhat.sso.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;
import com.redhat.sso.utils.MapBuilder;

public class ConfigOld {
  private static final Logger log = LoggerFactory.getLogger(Config.class);
  public static final File STORAGE_ROOT=new File("target/persistence");
  private static final File STORAGE=new File("target/persistence", "config.json");
  private static Config instance;
  private List<Map<String,Object>> list=null;
  private Map<String,String> options=null;
  private Map<String,Object> values=null;
  private List<Map<String,String>> notifications=null;
  
  
//  public ConfigOld(){}
//  public ConfigOld(String json){
////    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX="+json);
//    try{
//      Config x=Json.newObjectMapper(true).readValue(json, Config.class);
//      this.options=x.options;
//      this.list=x.list;
//      this.values=x.values;
//      this.notifications=x.notifications;
//      instance=this;
//    }catch(JsonProcessingException e){
//      e.printStackTrace();
//    }
//  }
  
  
  public Map<String,String> getOptions() {if (options==null) options=new HashMap<String, String>(); return options;}
  public List<Map<String,Object>> getList() {if (list==null) list=new ArrayList<Map<String, Object>>(); return list;}
  public Map<String,Object> getValues() {if (values==null) values=new HashMap<String, Object>(); return values;}
  public List<Map<String,String>> getNotifications() {if (notifications==null) notifications=new ArrayList<Map<String, String>>(); return notifications;}
  
  public static void main(String[] asd) throws IOException{
    Config c=Config.get();
    System.out.println(Json.newObjectMapper(true).writeValueAsString(Config.get()));
  }
  
  public void save(){
    try{
      if (!Config.STORAGE.getParentFile().exists()) Config.STORAGE.getParentFile().mkdirs();
      String json=Json.newObjectMapper(true).writeValueAsString(instance);
      IOUtils2.writeAndClose(json.getBytes(), new FileOutputStream(Config.STORAGE));
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static Config get(){
    if (instance==null){
      try{
        log.debug("Looking for config in: "+STORAGE.getAbsolutePath());
        if (!Config.STORAGE.exists()){
          if (!Config.STORAGE.getParentFile().exists()) Config.STORAGE.getParentFile().mkdirs();
          // copy the default config over
          IOUtils.copy(Config.class.getClassLoader().getResourceAsStream(STORAGE.getName()), new FileOutputStream(STORAGE));
        }
        instance=Json.newObjectMapper(true).readValue(Config.STORAGE, Config.class);
      }catch(Exception e){
        e.printStackTrace();
        instance=new Config();
      }
    }
    return instance;
  }
  
  public void setOptions(Map<String,String> value) {
    this.options=value;
  }
}


