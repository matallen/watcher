package com.redhat.sso.backup;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.sso.utils.SSLUtilities;

public class Heartbeat {
  private static final Logger log=MyLoggerFactory.getLogger(Heartbeat.class);
  private static Timer t;
  public static final Long startupDelay=30000l;

  public static void main(String[] asd){
    try{
      Heartbeat.runOnce();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static void runOnce(){
    new HeartbeatRunnable().run();
  }
  
  public static void start(long intervalInMs) {
    t = new Timer("backup-heartbeat", false);
    t.scheduleAtFixedRate(new HeartbeatRunnable(), startupDelay, intervalInMs);
    
//    String sslTrustAll=Config.get().getOptions().get("ssl.certs.trustall");
//    if ("true".equalsIgnoreCase(sslTrustAll)){
//    	log.debug("Trusting all hostnames & SSL certs");
////    SSLUtilities.trustAllHostnames();
//    	SSLUtilities.trustAllHttpsCertificates();
//    }
  }

  public static void stop() {
    t.cancel();
  }
  

  static class HeartbeatRunnable extends TimerTask {
    @Override
    public void run() {
//      log.info("Heartbeat fired");
//      new Backup().run(null); // null == backup all tasks
//      new Cleanup().run(null); // null == backup all tasks
    }      
  }

}
