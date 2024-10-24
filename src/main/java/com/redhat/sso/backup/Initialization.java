package com.redhat.sso.backup;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.sso.utils.MapBuilder;
import com.redhat.sso.utils.SSLUtilities;
import com.redhat.sso.utils.TimeUtils;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class Initialization {
  private static final Logger log=MyLoggerFactory.getLogger(Initialization.class);
  public static final String DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
  private final SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
  
  public void onStartup(@Observes StartupEvent ev) {
    long backupInterval=TimeUtils.sensibleStringToMs(Config.get().getOptions().get("backupInterval"));
//    System.out.println("cfg.backupInterval="+Config.get().getOptions().get("backupInterval"));
//    System.out.println("backupInterval="+backupInterval);
    log.debug("Starting Heartbeat with delay ("+Heartbeat.startupDelay+") and interval ("+TimeUtils.msToSensibleString(backupInterval)+")");
    Heartbeat.start(backupInterval);//TimeUnit.HOURS.toMillis(backupInterval));
    
    String sslTrustAll=Config.get().getOptions().get("ssl.certs.trustall");
    if ("true".equalsIgnoreCase(sslTrustAll)){
      log.debug("Trusting all hostnames & SSL certs");
//    SSLUtilities.trustAllHostnames();
      SSLUtilities.trustAllHttpsCertificates();
    }
    
    Database db=Database.get();
    
    // reset data on startup, so the health bar starts again
    db.getTasks().clear();
    db.save();
    
    // re-start all monitors
    boolean dbUpdated=false;
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
      System.out.println(String.format("adding monitor %s %s %s", name, (String)t.get("pingInterval"), enabled));
      ManagementController.monitors.put(name,
          Monitor.newInstance(
              name,
//              Long.parseLong((String)t.get("pingIntervalInMinutes")), 
              TimeUtils.sensibleStringToMs((String)t.get("pingInterval")),
              t.get("url")+"",
              enabled
          ));
    }
    if (dbUpdated) db.save();
  }
  
  void onShutdown(@Observes ShutdownEvent e) {
    log.info("Shutting down...");
    Heartbeat.stop();
//    PingSelf.stop();
  }
}