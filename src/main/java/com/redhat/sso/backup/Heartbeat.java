package com.redhat.sso.backup;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class Heartbeat {
  private static final Logger log = Logger.getLogger(Heartbeat.class);
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
  }

  public static void stop() {
    t.cancel();
  }
  

  static class HeartbeatRunnable extends TimerTask {
    @Override
    public void run() {
      log.info("Heartbeat fired");
      new Backup().run(null); // null == backup all tasks
    }      
  }

}
