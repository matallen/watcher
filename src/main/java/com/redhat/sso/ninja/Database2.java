package com.redhat.sso.ninja;

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

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;

public class Database2{
  private static final Logger log=Logger.getLogger(Database2.class);
  public static final String STORAGE="target/ninja-persistence/database2.json";
  public static final File STORAGE_AS_FILE=new File(STORAGE);
  public static Integer MAX_EVENT_ENTRIES=10000;
  
  
  private List<Map<String, String>> events;
  
  public static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  public static SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
  
  public List<Map<String, String>> getEvents(){
    if (null==events) events=new ArrayList<Map<String,String>>();
    return events;
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
      if (!new File(STORAGE).getParentFile().exists())
        new File(STORAGE).getParentFile().mkdirs();
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(new File(STORAGE)));
      log.info("Database saved ("+(System.currentTimeMillis()-s)+"ms)");
    }catch (JsonGenerationException e){
      e.printStackTrace();
    }catch (JsonMappingException e){
      e.printStackTrace();
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static synchronized Database2 load(){
    try{
      Database2 db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(STORAGE))), Database2.class);
      return db;
    }catch (JsonParseException e){
      e.printStackTrace();
    }catch (JsonMappingException e){
      e.printStackTrace();
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    return null;
  }
  
  private static Database2 instance=null;
  public static Database2 getCached(){
    if (null==instance){
      instance=Database2.get();
    }
    return instance;
  }
  public static Database2 get(){
    if (!new File(STORAGE).exists())
      new Database2().save();
    instance=Database2.load();
    return instance;
  }
  
}
