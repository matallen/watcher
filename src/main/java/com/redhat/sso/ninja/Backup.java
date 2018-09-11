package com.redhat.sso.ninja;

import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.redhat.sso.utils.DownloadFile;

public class Backup {
  private static final Logger log = Logger.getLogger(Backup.class);
  private static Timer t;

  public static void main(String[] asd){
    try{
      Backup.runOnce();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static void runOnce(){
    new BackupRunnable().run();
  }
  
  public static void start(long intervalInMs){//, String... paths) {
    t = new Timer(Backup.class.getSimpleName()+"-timer", false);
    t.scheduleAtFixedRate(new BackupRunnable(), 180000l, intervalInMs);
  }

  public static void stop() {
    t.cancel();
  }
  
  static class BackupRunnable extends TimerTask {
    static SimpleDateFormat sdf=new SimpleDateFormat(Config.get().getOptions().get("format")); //yyyy-MM-dd'T'HH:mm:ss
//    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public void run() {
      log.info(Backup.class.getSimpleName()+ " fired");
      
      Database2 db=Database2.get();
      
      File storage=new File("target/persistence");
      storage.mkdirs();
      
      for(Map<String, Object> destination:Config.get().getList()){
        String remoteLocation=(String)destination.get("url");
        File source=new File(storage, (String)destination.get("name"));
        
        String newName=FilenameUtils.getBaseName(source.getName())+"-"+sdf.format(new Date())+".bak";//+FilenameUtils.getExtension(source.getName());
        File localDestination=new File(source.getParentFile(), newName);
        
        
        try{
          new DownloadFile().get(remoteLocation, localDestination, PosixFilePermission.GROUP_READ);
          db.addEvent("Url Backup", (String)destination.get("name") +" @ "+ (String)destination.get("url") +" -> "+ newName);
//          System.out.println("Copying from ["+source.getAbsolutePath()+"] to ["+newFile.getAbsolutePath()+"]");
//          IOUtils.copy(new FileInputStream(source), new FileOutputStream(newFile));
        }catch(Exception e){
          e.printStackTrace();
        }
        
      }
      
      db.save();
      
    }      
  }

}
