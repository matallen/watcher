package com.redhat.sso.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;
import com.redhat.sso.utils.MapBuilder;
import com.redhat.sso.utils.TimeUtils;

@Path("/api")
public class ManagementController{
  private static final Logger log=MyLoggerFactory.getLogger(ManagementController.class);
//	public static List<Monitor> monitors=new ArrayList<Monitor>();
	public static Map<String, Monitor> monitors=new HashMap<String, Monitor>();
	
	public static void main(String[ ]asd){
		System.out.println(String.format("%5s", "").replaceAll(" ", "X"));
		Database.get();
		new Backup().run(null);
	}
	
	@DELETE @Path("/tasks/{task}/backups/{backup}/delete") public Response delete2(@PathParam("task") String task, @PathParam("backup") String backup) {
    File storage=new File(Config.STORAGE_ROOT, task);
    for(File f:storage.listFiles()){
      if (f.getName().equals(backup)){
        boolean result=f.delete();
        System.out.println("Removing file ("+(result?"successful":"FAILED")+"): "+f.getAbsolutePath());
      }else{
//        System.out.println("Not removing file: "+f.getAbsolutePath());
//        System.out.println("f.name="+f.getName());
//        System.out.println("backup="+backup);
      }
    }
    return Response.ok().build();
	}

	@POST @Path("/tasks/{task}/backupNow") public Response backupNow2(@PathParam("task") String task) {
    new Backup().run(task);
    return Response.status(200).build();
	}
	
  @POST @Path("/tasks/{task}/enabled/{enable}") public Response enabledMonitor2(@PathParam("task") String task, @PathParam("enable")String enable) throws FileNotFoundException, IOException {
    System.out.println("enabledMonitor called: taskName="+task+", enable="+enable);
    // find the config and update the config "enabled" setting
    Config cfg=Config.get();
    Map<String, Object> config=null;
    for(Map<String, Object> t:cfg.getList()){
      String name=t.get("name")+"";
      if (name.equals(task)){
        config=t;
        break;
      }
    }
    if (config==null) throw new RuntimeException("Unable to find task with name: "+task);
    config.put("enabled", enable);
    
    // activate/deactivate the monitor
    Monitor monitor=monitors.get(task);
    if (monitor!=null){
      // update existing monitor
      if ("true".equalsIgnoreCase(enable)){
        monitor.start(task, TimeUtils.sensibleStringToMs((String)config.get("pingInterval")));
      }else{
        monitor.stop();
      }
    }else{
      // create a new monitor
      monitors.put(task, Monitor.newInstance(
          task, 
          TimeUtils.sensibleStringToMs((String)config.get("pingInterval")),
          config.get("url")+"",
          "true".equalsIgnoreCase(String.valueOf(config.get("enabled")))
        ));
    }
    cfg.save();
    return Response.status(200).build();
  }
	
  @POST @Path("/config/options/{option}") public Response setConfigOption2(InputStream stream, @PathParam("option") String option) throws IOException {
    String payload=IOUtils.toString(stream, "UTF-8");
    log.debug(String.format("/config/options/%s: updating to %s", option, payload));
    Config cfg=Config.get();
//    cfg.getOptions().put(option, String.valueOf(payload.trim().equalsIgnoreCase("true") || payload.trim().equalsIgnoreCase("on") || payload.trim().equalsIgnoreCase("checked")) );
    cfg.getOptions().put(option, payload.trim() );
    cfg.save();
    return Response.status(200).build();
  }
  
  @GET @Path("/config/options/{option}") public Response getConfigOption2(@PathParam("option") String option) {
    return Response.status(200).entity(Config.get().getOptions().get(option)).build();
  }
  
  @GET @Path("/backups/{task}") public Response backups(@PathParam("task") String task) throws JsonProcessingException {
//    log.debug("tasks:: Called");
    File taskRootFolder=new File(Config.STORAGE_ROOT, task);
    // response entity structure only
    class Backup{
      String name; public String getName(){return name;}
      Long size; public Long getSize(){return size;}
      Backup(String name, /*String[] labels,*/ Long size){ this.name=name; /*this.labels=labels;*/ this.size=size; }
    }
    
    List<Backup> result=new ArrayList<Backup>();
    if (taskRootFolder.exists()){
      for(File f:taskRootFolder.listFiles()){
//        log.info("File:: "+f.getAbsolutePath()+" - size = "+f.length());
        result.add(new Backup(f.getName(), /*new String[]{},*/ f.length()));
      }
    }
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(result)).build();
  }  
	
  @GET @Path("tasks") public Response tasks() throws JsonProcessingException {
//    log.debug("tasks:: Called");
    
    // response entity structure only
    class Task{
      private String name;       public String getName(){return name;}
      private String enabled;    public String getEnabled(){return enabled;}
      private String status;     public String getStatus(){return status;}
      private String health;     public String getHealth(){return health;}
      private String backup;     public String getBackup(){return backup;}
//      private String sourceUrl;  public String getSourceUrl(){return sourceUrl;}
//      private String hostedUrl;  public String getHostedUrl(){return hostedUrl;}
      private Map<String,String> info;  public Map<String,String> getInfo(){return info;}
      public Task(String name, String enabled, String status, String health, String backup, Map<String,String> info/*, String sourceUrl, String hostedUrl*/){ this.name=name; this.enabled=enabled; this.status=status; this.health=health; this.backup=backup; this.info=info; /*this.sourceUrl=sourceUrl; this.hostedUrl=hostedUrl;*/ }
    }
    List<Task> result=new ArrayList<Task>();
    
    int healthBlockSize=(null!=Config.get().getOptions().get("health.block.size")?Integer.parseInt(Config.get().getOptions().get("health.block.size")):20); // 20 is the default
    
    Database db=Database.get();
    boolean dbUpdated=false;
    for(Map<String, Object> c:Config.get().getList()){
      String name=(String)c.get("name");
      
      // should centralize this method as it's used/copy-pasted elsewhere
      if (!db.getTasks().containsKey(name)){
        db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%"+healthBlockSize+"s", "").replaceAll(" ", "X")).build());
        dbUpdated=true;
      }

      Map<String, String> task=db.getTasks().get(name);
      
      Map<String, String> info=new HashMap<String, String>();
      info.put("Ping URL", (String)c.get("url"));
      info.put("Source URL", (String)c.get("info-sourceUrl"));
      info.put("Hosted URL", (String)c.get("info-hostedUrl"));
//      info.put("Is it backing up?", (String)c.get("backup"));
      info.put("Ping Interval", (String)c.get("pingInterval"));
      
      
      Monitor.resizeHealth(task, healthBlockSize);
      
      result.add(new Task(name, (String)c.get("enabled"), task.get("status"), task.get("health"), (String)c.get("backup"), info/*, (String)c.get("info-sourceUrl"), (String)c.get("info-hostedUrl")*/));
    }
    
    if (dbUpdated) db.save();
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(result)).build();
  }

  @GET @Path("/config") public Response getConfig() throws JsonProcessingException{ // return config file contents
    log.debug("GetConfig:: Called");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  @POST @Path("/config") public Response saveConfig(InputStream stream) throws IOException{ // save config file contents
    log.debug("SaveConfig:: Called");
    String payload=IOUtils.toString(stream, "UTF-8");
//    System.out.println("payload = "+payload);
    
    Config newConfig=Json.newObjectMapper(true).readValue(payload, Config.class);

    log.debug("SaveConfig:: New Config = " + Json.newObjectMapper(true).writeValueAsString(newConfig));
    newConfig.save();

    // re-start the heartbeat with a new interval
    if (null!=newConfig.getOptions().get("backupInterval")){
      long backupInterval=TimeUtils.sensibleStringToMs(newConfig.getOptions().get("backupInterval"));
      log.info("SaveConfig:: Re-setting heartbeat with interval: " + TimeUtils.msToSensibleString(backupInterval));
      Heartbeat.stop();
      Heartbeat.start(backupInterval);
    }

    // re-start the ping with a new interval
//    long pingIntervalInMs=3600l;
    if (null!=newConfig.getOptions().get("pingInterval")){
      long pingInterval=TimeUtils.sensibleStringToMs(newConfig.getOptions().get("pingInterval"));
      log.info("SaveConfig:: Re-setting ping with interval: " + TimeUtils.msToSensibleString(pingInterval));
//      PingSelf.stop();
//      PingSelf.start(pingIntervalInMs);
    }
    
    // re-start all monitors
    for(Monitor m:monitors.values()) m.stop();
    for(Map<String, Object> t:newConfig.getList()){
      String name=String.valueOf(t.get("name"));
      monitors.put(name, Monitor.newInstance(
          name, 
          TimeUtils.sensibleStringToMs((String)t.get("pingInterval")),
          t.get("url")+"",
          "true".equalsIgnoreCase(String.valueOf(t.get("enabled")))
        ));
    }

    String maxEvents=newConfig.getOptions().get("maxEvents");
    if (null != maxEvents && maxEvents.matches("\\d+")){
      Database.MAX_EVENT_ENTRIES=Integer.parseInt(maxEvents);
    }

    log.debug("SaveConfig:: Complete");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }

  @GET @Path("/events") public Response getEvents() throws JsonProcessingException{
    log.debug("GetEvents:: Called");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get().getEvents())).build();
  }
  
  @GET @Path("/database") public Response getDatabase() throws JsonProcessingException{
    log.debug("GetDatabase:: Called");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get())).build();
  }
  @POST @Path("/database") public Response saveDatabase(InputStream stream) throws IOException{
    log.debug("SaveDatabase:: Called");
    Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(stream), new TypeReference<Database>(){});
    log.debug("SaveDatabase:: New DB = " + Json.newObjectMapper(true).writeValueAsString(db));
    db.save();
    log.debug("SaveDatabase:: Complete");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get())).build();
  }
  
  @GET @Path("/download") @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFile(@QueryParam("file") String filename) throws IOException{
    log.debug("GetFile:: Called");
//    String filename=request.getParameter("file");
    File file=new File(Config.STORAGE_ROOT, filename);
    return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
        .build();
  }
}