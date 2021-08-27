package com.redhat.sso.backup;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.redhat.sso.backup.AlertGoogleChat.ChatEvent;
import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.MapBuilder;
import com.redhat.sso.utils.Http.Response;

public class Monitor{
  private static final Logger log = Logger.getLogger(Monitor.class);
  private Timer t;
  private String name;
  private String url;
  
  public Monitor(){}
  public Monitor(String name, String url){
  	this.name=name;
  	this.url=url;
  }
  
  public static Monitor newInstance(String name, long intervalInMins, String url, boolean enabled){
  	Monitor m=new Monitor(name, url);
  	if (enabled){
//  		System.out.println("starting timer for :"+name);
  		m.start(name, TimeUnit.MINUTES.toMillis(intervalInMins));
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
    public void run() {
      r(name,url);
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
			System.out.println("init: "+name+" was found");
		}
		return dataChanged;
  }
  
  public static synchronized void r(String name, String url){
//    log.info(name+" fired");
    
		Response response=Http.get(url);
		log.info("Monitor ("+name+"): called '" + url + "', response code was: " + response.responseCode);
    
		Database db=Database.get();
		// should centralize this method as it's used/copy-pasted elsewhere
  	if (!db.getTasks().containsKey(name)){
  		db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%20s", "").replaceAll(" ", "X")).build());
//  		dbUpdated=true;
  	}

//		if (new Monitor().initTaskIfNecessary(db, name));
			
		Map<String, String> data=db.getTasks().get(name);
		data.put("status", String.valueOf(response.responseCode));
		
		// make a decision on the response that represents the status
		String decision="";
		if (response.responseCode>=200 && response.responseCode<=299){ // Success
			decision="U";
		}else if (response.responseCode>=300 && response.responseCode<=399){ // redirections
			decision="T";
		}else if (response.responseCode>=400 && response.responseCode<=499){ // errors
			decision="D";
		}else if (response.responseCode>=500 && response.responseCode<=599){ // server errors
			decision="T";
		}else{
			decision="X";
		}
		
		data.put("status", decision+"|"+String.valueOf(response.responseCode));
		
		// store the health timeline indicator
		data.put("health", data.get("health").substring(1)+decision);
		db.save();
		
		if (decision.matches("[D|T]")){
			new AlertSlack().alert(name, url, response.responseCode);
			new AlertGoogleChat().send(ChatEvent.onHttpFailure, name, url, String.valueOf(response.responseCode));
		}
  }
}
