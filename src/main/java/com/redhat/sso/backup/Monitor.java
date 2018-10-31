package com.redhat.sso.backup;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.redhat.sso.backup.Heartbeat.HeartbeatRunnable;

public class Monitor{
  private static final Logger log = Logger.getLogger(Monitor.class);
  private static Timer t;
  
  public static void runOnce(){
    new HeartbeatRunnable().run();
  }
  
  public static void start(long intervalInMs) {
		long startupDelay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.of(13, 5, 45));
    t = new Timer("backup-heartbeat", false);
    t.scheduleAtFixedRate(new HeartbeatRunnable(), startupDelay, intervalInMs);
  }

  public static void stop() {
    t.cancel();
  }

  static class MonitorRunnable extends TimerTask {
    @Override
    public void run() {
//      log.info("Heartbeat fired");
      new Backup().run();
    }      
  }
}
