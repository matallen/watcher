package com.redhat.sso.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadFile{
  private static final Logger log = LoggerFactory.getLogger(DownloadFile.class);
  
  public static void main(String[] asd) throws IOException{
    String remoteLocation="http://localhost:8082/community-ninja-board/api/script/github-stats.py -s ABCDEF";
    File localDestination=new File("/home/mallen/Work/poc/sso-tools/community-ninja-board/target/temp");
    PosixFilePermission permissions=PosixFilePermission.GROUP_EXECUTE;
    System.out.println(new DownloadFile().get(remoteLocation, localDestination, permissions));
  }
  
  public String get(String remoteLocation, String localDestination, PosixFilePermission... permissions) throws IOException{
    return get(remoteLocation, new File(localDestination), permissions);
  }
  
  public String get(String remoteLocation, File localDestination, PosixFilePermission... permissions) throws IOException{
    
      log.debug("Provided remote location is: "+remoteLocation);
      localDestination.getParentFile().mkdirs();
      URL sanitizedRemoteLocation=new URL(remoteLocation.contains(" ")?remoteLocation.substring(0, remoteLocation.indexOf(" ")):remoteLocation);
      
//      File dest=new File(localDestination, new File(sanitizedRemoteLocation.getPath()).getName()); // extract just the name, not the path
      File dest=localDestination;
      
      boolean hasExecutablePermissions=dest.exists() && Files.getPosixFilePermissions(dest.toPath()).contains(PosixFilePermission.GROUP_EXECUTE);
      
      if (!dest.exists() || !hasExecutablePermissions){ // then its not been downloaded yet, so go get it
        
        log.debug("Downloading from ["+sanitizedRemoteLocation+"] to ["+dest.getAbsolutePath()+"]");
        if (dest.exists()) dest.delete();
        FileOutputStream os=new FileOutputStream(dest);
        try{
          IOUtils.copy(sanitizedRemoteLocation.openStream(), os);
        }finally{
          os.close();
        }
        
        FilePermissions.set(dest, 
            PosixFilePermission.OWNER_READ, 
            PosixFilePermission.OWNER_WRITE, 
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ, 
            PosixFilePermission.GROUP_WRITE, 
            PosixFilePermission.GROUP_EXECUTE
            );
        
        Files.getPosixFilePermissions(dest.toPath()).contains(PosixFilePermission.GROUP_EXECUTE);
        
      }else{
        log.debug("file exists, not downloading: "+dest.getAbsolutePath());
      }
//      String result=dest.getAbsolutePath() + (remoteLocation.contains(" ")?remoteLocation.substring(remoteLocation.indexOf(" ")):"");
//      log.debug("New remote location is: "+result);
      return dest.getAbsolutePath();
//      log.debug("command is now: "+command);
//    }
  }
}
