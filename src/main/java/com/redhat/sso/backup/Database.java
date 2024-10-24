package com.redhat.sso.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;

public class Database{
  private static final Logger log=MyLoggerFactory.getLogger(Database.class);
//  public static final String STORAGE="target/ninja-persistence/";
//  public static final File STORAGE_AS_FILE=new File(STORAGE);
  public static final File STORAGE=new File(Config.STORAGE_ROOT, "database.json");
  public static Integer MAX_EVENT_ENTRIES=10000;
  
  private List<Map<String, String>> events;
  private Map<String,Map<String, String>> tasks;
  
  public static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//  public static SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
  
  public List<Map<String, String>> getEvents(){
    if (null==events) events=new ArrayList<Map<String,String>>();
    return events;
  }
  public Map<String, Map<String, String>> getTasks(){
    if (null==tasks) tasks=new HashMap<String, Map<String,String>>();
    return tasks;
  }
  
  public void addEvent(String type, String text){
    Map<String,String> event=new HashMap<String, String>();
    event.put("timestamp", sdf.format(new Date()));
    event.put("type", type);
    event.put("text", text);
    getEvents().add(event);
    
    // limit the events to 100 entries
    while (getEvents().size()>MAX_EVENT_ENTRIES){
      getEvents().remove(0);
    }
  }
  
  
  public synchronized void save(){
    try{
      long s=System.currentTimeMillis();
      if (!STORAGE.getParentFile().exists())
      	STORAGE.getParentFile().mkdirs();
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(STORAGE));
      log.trace("Database saved ("+(System.currentTimeMillis()-s)+"ms)");
//      log.info("database = "+Json.toJson(this));
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static synchronized Database load(){
    try{
      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(STORAGE)), Database.class);
      return db;
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    return null;
  }
  
  private static Database instance=null;
  public static Database getCached(){
    if (null==instance){
      instance=Database.get();
    }
    return instance;
  }
  public static Database get(){
    if (instance!=null) return instance;
    if (!STORAGE.exists())
      new Database().save();
    instance=Database.load();
    return instance;
  }
  
}
