package com.redhat.sso.backup;

import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import com.redhat.sso.utils.DownloadFile;

public class Backup {
  private static final Logger log=MyLoggerFactory.getLogger(Backup.class);
  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  public void run(String taskToBackup) {
    log.info(Backup.class.getSimpleName()+ " fired");
    
    Database db=Database.get();
    
    Config.STORAGE_ROOT.mkdirs();
    
    for(Map<String, Object> destination:Config.get().getList()){
    	
    	if (null==taskToBackup || destination.get("name").equals(taskToBackup)){
    		if (null!=destination.get("backup") && ("false".equalsIgnoreCase((String)destination.get("backup")))) continue;
    		
    		log.info("Backing up: "+destination.get("name"));
    		
    		String remoteLocation=(String)destination.get("url");
    		File source=new File(Config.STORAGE_ROOT, (String)destination.get("name"));
    		
    		String newName=FilenameUtils.getBaseName(source.getName())+"-"+sdf.format(new Date())+(taskToBackup!=null?"-[explicit]":"")+".bak";//+FilenameUtils.getExtension(source.getName());
    		File localDestination=new File(source, newName);
    		
    		try{
    			new DownloadFile().get(remoteLocation, localDestination, PosixFilePermission.GROUP_READ);
    			
    			db.addEvent("Backup SUCCESS", (String)destination.get("name") +" @ "+ (String)destination.get("url") +" -> "+ newName);
    			log.debug("Backup success - "+taskToBackup);
//          System.out.println("Copying from ["+source.getAbsolutePath()+"] to ["+newFile.getAbsolutePath()+"]");
//          IOUtils.copy(new FileInputStream(source), new FileOutputStream(newFile));
    		}catch(Exception e){
    			db.addEvent("Backup ERROR", (String)destination.get("name") +" @ "+ (String)destination.get("url") +" -> ErrorMessage: "+e.getMessage());
//    			e.printStackTrace();
    			log.debug("Backup failure - "+taskToBackup+" - "+e.getMessage());
    		}
    	}
      
    }
    
    db.save();
    
  }      

}
