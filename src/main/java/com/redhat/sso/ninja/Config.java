package com.redhat.sso.ninja;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;
import com.redhat.sso.utils.MapBuilder;

public class Config {
  private static final Logger log=Logger.getLogger(Config.class);
  public static final File STORAGE=new File("target/persistence", "config.json");
  private static Config instance;
  private List<Map<String,Object>> list=null;
  private Map<String,String> options=null;
  private Map<String,Object> values=null;
  
  
  public Config(){}
  public Config(String json){
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX="+json);
    try{
      Config x=Json.newObjectMapper(true).readValue(json, Config.class);
      this.options=x.options;
//      this.scripts=x.scripts;
      this.values=x.values;
      instance=this;
    }catch (JsonParseException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (JsonMappingException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (IOException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  
  public Map<String,String> getOptions() {if (options==null) options=new HashMap<String, String>(); return options;}
  public List<Map<String,Object>> getList() {if (list==null) list=new ArrayList<Map<String, Object>>(); return list;}
  public Map<String,Object> getValues() {if (values==null) values=new HashMap<String, Object>(); return values;}
  
//  class MapBuilder<K,V>{
//    Map<K, V> values=new HashMap<K, V>();
//    public MapBuilder<K,V> put(K key, V value){
//      values.put(key, value); return this;
//    }
//    public Map<K, V> build(){
//      return values;
//    }
//  }
  
  public static void main(String[] asd){
    Config c=Config.get();
//    c.getOptions().put("sources", "com.redhat.sso.ninja.TrelloSync");
    
//    c.getScripts().add(new MapBuilder<String, Object>()
//        .put("source", "com.redhat.sso.ninja.TrelloSync")
//        .put("type", "class")
//        .put("options", new MapBuilder<String, String>().put("organizationName","redhatcop").build())
//        .build());
//    c.getScripts().add(new MapBuilder<String, Object>().put("source", "/home/mallen/poc/script1.perl").put("type", "perl").build());
//    c.getScripts().add(new MapBuilder<String, Object>().put("source", "/home/mallen/poc/script2.sh").put("type", "bash").build());
//    c.getScripts().put("list", scripts);
    
    c.getList().clear();
    c.getList().add(new MapBuilder<String, Object>()
      .put("name", "ninja")
      .put("url", "https://community-ninja-board-community-ninja-board.apps.d1.casl.rht-labs.com/community-ninja-board/api/database/get")
//      .put("intervalInHours", 24l)
//      .put("options", new MapBuilder<String, String>().put("organizationName","redhatcop").build())
      .build());
    
//    Map<String, String> scripts=new HashMap<String, String>();
//    scripts.put("doX", "/home/mallen/poc/script1.perl");
    
    c.getOptions().put("intervalInHours", "24");
    c.getOptions().put("format", "yyyy-MM-dd'T'HHmmss");
    c.getOptions().put("maxEvents", "10000");
//    c.getValues().put("lastRun", 1520660463301l);
    c.save();
  }
  
  public void save(){
    try{
      if (!Config.STORAGE.getParentFile().exists()) Config.STORAGE.getParentFile().mkdirs();
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsString(instance).getBytes(), new FileOutputStream(Config.STORAGE));
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
        String toLoad=IOUtils2.toStringAndClose(new FileInputStream(Config.STORAGE));
        instance=Json.newObjectMapper(true).readValue(new ByteArrayInputStream(toLoad.getBytes()), Config.class);
        
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


