package com.redhat.sso.backup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Heartbeat {
  private static final Logger log = Logger.getLogger(Heartbeat.class);
  private static Timer t;
  private static final Long startupDelay=30000l;

  public static void main(String[] asd){
    try{
      Heartbeat.runOnce();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
//  public static String convertLastRun(String command, Date lastRunDate) throws ParseException {
//    Matcher m=Pattern.compile("(\\$\\{([^}]+)\\})").matcher(command);
//    StringBuffer sb=new StringBuffer();
//    while (m.find()){
//      String toReplace=m.group(2);
//      if (toReplace.contains("LAST_RUN:")){
//        SimpleDateFormat sdf=new SimpleDateFormat(toReplace.split(":")[1].replaceAll("}", "")); // nasty replaceall when I just want to trim the last char
//        m.appendReplacement(sb, sdf.format(lastRunDate));
//        
//      }else if (toReplace.contains("DAYS_FROM_LAST_RUN")){
//        Date runTo2=java.sql.Date.valueOf(LocalDate.now());
////        Calendar runTo=Calendar.getInstance();
////        runTo.setTime(new Date());
////        runTo.set(Calendar.HOUR, 0);
////        runTo.set(Calendar.MINUTE, 0);
////        runTo.set(Calendar.SECOND, 0);
//        Integer daysFromLastRun=(int)((runTo2.getTime() - lastRunDate.getTime()) / (1000 * 60 * 60 * 24))+1;
//        m.appendReplacement(sb, String.valueOf(daysFromLastRun));
//      }else{
//        // is it a system property?
//        if (null!=System.getProperty(toReplace)){
//          m.appendReplacement(sb, System.getProperty(toReplace));
//        }else{
//          m.appendReplacement(sb, "?????");
//        }
//      }
//    }
//    m.appendTail(sb);
//    return sb.toString();
//  }
  
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
      new Backup().run();
    }      
  }

}
