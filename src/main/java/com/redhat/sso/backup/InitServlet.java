package com.redhat.sso.backup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.redhat.sso.utils.MapBuilder;

public class InitServlet extends HttpServlet {
	private static final Logger log=Logger.getLogger(InitServlet.class);
	
  public static void main(String[] asd) throws ServletException{
    new InitServlet().init(null);
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    long pingIntervalInMs=3600l;
    if (null!=Config.get().getOptions().get("pingIntervalInMinutes"))
    	pingIntervalInMs=TimeUnit.MINUTES.toMillis(Long.parseLong(Config.get().getOptions().get("pingIntervalInMinutes")));
    
    log.debug("Starting PingSelf with delay ("+PingSelf.startupDelay+") and interval ("+pingIntervalInMs+"ms)");
    PingSelf.start(pingIntervalInMs);
 		
 		long intervalInHours=Long.parseLong(Config.get().getOptions().get("intervalInHours"));
 		log.debug("Starting Heartbeat with delay ("+Heartbeat.startupDelay+") and interval ("+intervalInHours+"h)");
    Heartbeat.start(TimeUnit.HOURS.toMillis(intervalInHours));
    
    // re-start all monitors
    boolean dbUpdated=false;
    Database db=Database.get();
    for(Monitor m:ManagementController.monitors.values())
    	m.stop();
    for(Map<String, Object> t:Config.get().getList()){
    	String name=String.valueOf(t.get("name"));
    	
			// should centralize this method as it's used/copy-pasted elsewhere
    	if (!db.getTasks().containsKey(name)){
    		db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%20s", "").replaceAll(" ", "X")).build());
    		dbUpdated=true;
    	}
    	
    	boolean enabled="true".equalsIgnoreCase(String.valueOf(t.get("enabled")));
    	ManagementController.monitors.put(name,
    			Monitor.newInstance(
    					name,
    					Long.parseLong((String)t.get("pingIntervalInMinutes")), 
    					t.get("url")+"",
    					enabled
    			));
//    	if (enabled)
//    		System.out.println("Started monitor for: "+name);
    	
    	// reset data on startup, so the health bar starts again
    	db.getTasks().clear();
    	
    }
    
    
    
    if (dbUpdated)
    	db.save();
  }

  @Override
  public void destroy() {
    super.destroy();
    Heartbeat.stop();
    PingSelf.stop();
  }

}