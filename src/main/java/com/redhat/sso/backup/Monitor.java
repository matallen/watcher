package com.redhat.sso.backup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.sso.backup.AlertChat.ChatEvent;
import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.Json;
import com.redhat.sso.utils.MapBuilder;

import io.vertx.ext.web.handler.HttpException;


public class Monitor{
  private static final Logger log = LoggerFactory.getLogger(Monitor.class);
  private Timer t;
  private String name;
  private String url;
//  private static int healthBlockSize=(null!=Config.get().getOptions().get("health.block.size")?Integer.parseInt(Config.get().getOptions().get("health.block.size")):20);
  
  public Monitor(){}
  public Monitor(String name, String url){
  	this.name=name;
  	this.url=url;
  }
  
  public static Monitor newInstance(String name, long intervalInMs, String url, boolean enabled){
  	Monitor m=new Monitor(name, url);
  	if (enabled){
//  		System.out.println("starting timer for :"+name);
//  		m.start(name, TimeUnit.MINUTES.toMillis(intervalInMins));
  		m.start(name, intervalInMs);
  	}
  	return m;
  }
  
  public void start(String name, long intervalInMs) {
//		long startupDelay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.of(13, 5, 45));
  	long startupDelay=0;
    t = new Timer("monitor:"+name, false);
    t.scheduleAtFixedRate(new MonitorRunnable(), startupDelay, intervalInMs);
  }

  public void stop(){
  	if (t!=null){
  		t.cancel();
  	}else{
  		log.warn("("+name+") Timer \"Stop\" called, but no timer was running");
  	}
  }

  class MonitorRunnable extends TimerTask {
    @Override
    public void run(){
      try{
        r(name,url);
      }catch(Exception ignoreSoThreadDoesntDie){}
    }      
  }
  
  //Initialise task if necessary
  public boolean initTaskIfNecessary(Database db, String name){
  	boolean dataChanged=false;
		if (null==db.getTasks().get(name)){ // lazy init the task
			System.out.println("init: "+name+" not found, initializing it");
			db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%20s", "").replaceAll(" ", "X")).build());
			dataChanged=true;
		}else{
//			System.out.println("init: "+name+" was found");
		}
		return dataChanged;
  }
  
  public static void resizeHealth(Map<String, String> data, int healthBlockSize){
		// resize the health block if necessary
		if (data.get("health").length()>healthBlockSize) data.put("health", data.get("health").substring(0, healthBlockSize));
		if (data.get("health").length()<healthBlockSize) data.put("health", String.format("%"+(healthBlockSize-data.get("health").length())+"s", "").replaceAll(" ", "X") + data.get("health"));

  }
  
  public synchronized void r(String name, String url){
//    log.info(name+" checking...");
    String decision="";
    String status="";
    Database db=Database.get();
    if (db==null){log.error(String.format("[%s] database is null"),name); return;}
//    initTaskIfNecessary(db, name);
    Map<String, String> data=db.getTasks().get(name);
    int healthBlockSize=10;
    try{
      healthBlockSize=Integer.parseInt(Config.get().getOptions().get("health.block.size"));
    }catch(Exception e){}
    if (data==null) return;
//    int healthBlockSize=(null!=Config.get().getOptions().get("health.block.size")?Integer.parseInt(Config.get().getOptions().get("health.block.size")):20); // 20 is the default

    // should centralize this method as it's used/copy-pasted elsewhere
    if (!db.getTasks().containsKey(name))
      db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%"+healthBlockSize+"s", "").replaceAll(" ", "X")).build());
    
    try{
      Http.get(url);
      decision="U";
      status="200";
      log.info(String.format("[%s] %s", status, url));// + response.responseCode);
    }catch(HttpException e){
      /*
       * if (e.getStatusCode()>=200 && e.getStatusCode()<=299){ // Success decision="U"; }else
       */
      if (e.getStatusCode()>=300 && e.getStatusCode()<=399){ // redirections
        decision="T";
      }else if (e.getStatusCode()>=400 && e.getStatusCode()<=499){ // errors
        decision="D";
      }else if (e.getStatusCode()>=500 && e.getStatusCode()<=599){ // server errors
        decision="T";
      }else{
        decision="X";
      }
//      statusCode=e.getStatusCode();
      status=String.valueOf(e.getStatusCode());
      log.info(String.format("[%s] %s", status, url));// + response.responseCode);
    }catch(UnknownHostException e){  decision="T"; status=e.getClass().getSimpleName().replace("Exception",""); log.info(String.format("[%s] %s", status, url));
    }catch(MalformedURLException e){ decision="T"; status=e.getClass().getSimpleName().replace("Exception",""); log.info(String.format("[%s] %s", status, url));
    }catch(IOException e){           decision="T"; status=e.getClass().getSimpleName().replace("Exception",""); log.info(String.format("[%s] %s", status, url));
    }
    
		
		// make a decision on the response that represents the status
//		String decision="";
//		if (response.responseCode>=200 && response.responseCode<=299){ // Success
//			decision="U";
//		}else if (response.responseCode>=300 && response.responseCode<=399){ // redirections
//			decision="T";
//		}else if (response.responseCode>=400 && response.responseCode<=499){ // errors
//			decision="D";
//		}else if (response.responseCode>=500 && response.responseCode<=599){ // server errors
//			decision="T";
//		}else{
//			decision="X";
//		}
		
		//data.put("status", decision+"|"+String.valueOf(statusCode));
    data.put("status", decision+"|"+String.valueOf(status));
    resizeHealth(data, healthBlockSize);
		// store the health timeline indicator
		data.put("health", data.get("health").substring(1)+decision);
//		try{
//		log.info("updated status for "+name+" - "+Json.toJson(data));
//		}catch(JsonProcessingException e){e.printStackTrace();}
		db.save();
		
		if (decision.matches("[D|T]")){
		  new AlertChat().send(ChatEvent.onHttpFailure, name, url, status);
//			new AlertSlack().alert(name, url, response.responseCode);
//			new AlertGoogleChat().send(ChatEvent.onHttpFailure, name, url, String.valueOf(status));
		}
  }
}
