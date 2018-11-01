package com.redhat.sso.backup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

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
    
    for(Monitor m:ManagementController.monitors)
    	m.stop();
    
    for(Map<String, Object> t:Config.get().getList()){
    	ManagementController.monitors.add(Monitor.newInstance(t.get("name")+"", Long.parseLong((String)t.get("pingIntervalInMinutes")), t.get("url")+""));
    }    
    
    
  }

  @Override
  public void destroy() {
    super.destroy();
    Heartbeat.stop();
    PingSelf.stop();
  }

}