package com.redhat.sso.backup;

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
    
    long intervalInHours=Long.parseLong(Config.get().getOptions().get("intervalInHours"));
 		long pingIntervalInMinutes=Long.parseLong(Config.get().getOptions().get("pingIntervalInMinutes"));
    
 		log.debug("Starting Heartbeat with delay ("+Heartbeat.startupDelay+") and interval ("+intervalInHours+")");
 		log.debug("Starting PingSelf with delay ("+PingSelf.startupDelay+") and interval ("+pingIntervalInMinutes+"m)");
 		
    Heartbeat.start(TimeUnit.HOURS.toMillis(intervalInHours));
    PingSelf.start(TimeUnit.MINUTES.toMillis(pingIntervalInMinutes));
  }

  @Override
  public void destroy() {
    super.destroy();
    Heartbeat.stop();
    PingSelf.stop();
  }

}