package com.redhat.sso.backup;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.MapBuilder;
import com.redhat.sso.utils.Http.Response;

//import com.redhat.sso.backup.Heartbeat.HeartbeatRunnable;

public class Monitor{
  private static final Logger log = Logger.getLogger(Monitor.class);
  private Timer t;
  private String name;
  private String url;
  
//  public static void runOnce(){
//    new HeartbeatRunnable().run();
//  }
  
  public Monitor(String name, String url){
  	this.name=name;
  	this.url=url;
  }
  
  public static Monitor newInstance(String name, long intervalInMins, String url){
  	Monitor m=new Monitor(name, url);
  	m.start(name, TimeUnit.MINUTES.toMillis(intervalInMins));
  	return m;
  }
  
  public void start(String name, long intervalInMs) {
//		long startupDelay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.of(13, 5, 45));
  	long startupDelay=0;
    t = new Timer("monitor:"+name, false);
    t.scheduleAtFixedRate(new MonitorRunnable(), startupDelay, intervalInMs);
  }

  public void stop(){
    t.cancel();
  }

  class MonitorRunnable extends TimerTask {
    @Override
    public void run() {
      r(name,url);
    }      
  }
  
  //Initialise task if necessary
  public static boolean initTaskIfNecessary(Database db, String name){
  	boolean dataChanged=false;
		if (null==db.getTasks().get(name)){ // lazy init the task
			db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "Unknown").put("health", String.format("%20s", "").replaceAll(" ", "X")).build());
			dataChanged=true;
		}
		return dataChanged;
  }
  
  public static synchronized void r(String name, String url){
//    log.info(name+" fired");
    
		Response response=Http.get(url);
		log.info("Monitor ("+name+"): called '" + url + "', response code was: " + response.responseCode);
    
		Database db=Database.get();
		if (initTaskIfNecessary(db, name));
			
		Map<String, String> data=db.getTasks().get(name);
		data.put("status", String.valueOf(response.responseCode));
		
		boolean error=false;
		if (200==response.responseCode){
			data.put("status", "U"); // service is UP
		}else if (response.responseCode>=500){
			data.put("status", "T"); // service timeout
		}else{
			data.put("status", "D"); // service is DOWN! alert!!!
			error=true;
		}
		data.put("health", data.get("health").substring(1)+data.get("status"));
		db.save();
		
		if (error && "true".equalsIgnoreCase(Config.get().getOptions().get("slack.webhook.notifications"))){
			String slackTemplate=Config.get().getOptions().get("slack.webhook.template");
			String slackUrl=Config.get().getOptions().get("slack.webhook.url");
			Http.post(slackUrl, slackTemplate.replaceAll("SERVER_NAME", name).replaceAll("SERVER_URL", url));
		}
  }
}
