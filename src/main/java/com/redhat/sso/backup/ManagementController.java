package com.redhat.sso.backup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.redhat.sso.utils.IOUtils2;
import com.redhat.sso.utils.Json;
import com.redhat.sso.utils.MapBuilder;

@Path("/")
public class ManagementController{
	private static final Logger log=Logger.getLogger(ManagementController.class);
//	public static List<Monitor> monitors=new ArrayList<Monitor>();
	public static Map<String, Monitor> monitors=new HashMap<String, Monitor>();
	
	
	public static void main(String[ ]asd){
		System.out.println(String.format("%5s", "").replaceAll(" ", "X"));
	}
	
	@POST
	@Path("/tasks/{task}/enabled/{enable}")
	public Response enabledMonitor(@PathParam("task")String taskName, @PathParam("enable")String enable, @Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		System.out.println("enabledMonitor called: taskName="+taskName+", enable="+enable);
		// find the config and update the config "enabled" setting
		Config cfg=Config.get();
		Map<String, Object> config=null;
		for(Map<String, Object> t:cfg.getList()){
			String name=t.get("name")+"";
			if (name.equals(taskName)){
				config=t;
				break;
			}
		}
		if (config==null) throw new RuntimeException("Unable to find task with name: "+taskName);
		config.put("enabled", enable);
		
		// activate/deactivate the monitor
		Monitor monitor=monitors.get(taskName);
		if (monitor!=null){
			// update existing monitor
			if ("true".equalsIgnoreCase(enable)){
				monitor.start(taskName, TimeUnit.MINUTES.toMillis(Long.parseLong((String)config.get("pingIntervalInMinutes"))));
			}else{
				monitor.stop();
			}
		}else{
			// create a new monitor
			monitors.put(taskName, Monitor.newInstance(
					taskName, 
    			Long.parseLong((String)config.get("pingIntervalInMinutes")), 
    			config.get("url")+"",
    			"true".equalsIgnoreCase(String.valueOf(config.get("enabled")))
    		));
		}
		
		cfg.save();

		return Response.status(200).build();
	}
	
	@POST
	@Path("/config/options/{option}")
	public Response setConfigOption(@PathParam("option")String option , @Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		String payload=IOUtils.toString(request.getInputStream());
		Config cfg=Config.get();
		cfg.getOptions().put("slack.webhook.notifications", String.valueOf(payload.trim().equalsIgnoreCase("true") || payload.trim().equalsIgnoreCase("on") || payload.trim().equalsIgnoreCase("checked")) );
		cfg.save();
		return Response.status(200).build();
	}
	
	@GET
	@Path("/config/options/{option}")
	public Response getConfigOption(@PathParam("option")String option , @Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		return Response.status(200).entity(Config.get().getOptions().get("slack.webhook.notifications")).build();
	}
	
	@GET
	@Path("/backups/{task}")
	public Response backups(@PathParam("task") String task, @Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("tasks:: Called");
		
		File taskRootFolder=new File(Config.STORAGE_ROOT, task);
		
		// response entity structure only
		class Backup{
			private String name; public String getName(){return name;}
			private String file; public String getFile(){return file;}
			public Backup(String name, String file){ this.name=name; this.file=file; }
		}
		
		List<Backup> result=new ArrayList<Backup>();
		if (taskRootFolder.exists()){
			for(File f:taskRootFolder.listFiles()){
				result.add(new Backup(f.getName(), ""));
			}
		}
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(result)).build();
	}
	
	@GET
	@Path("/tasks")
	public Response tasks(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("tasks:: Called");
		
	  // response entity structure only
		class Task{
			private String name;       public String getName(){return name;}
			private String enabled;    public String getEnabled(){return enabled;}
			private String status;     public String getStatus(){return status;}
			private String health;     public String getHealth(){return health;}
//			private String sourceUrl;  public String getSourceUrl(){return sourceUrl;}
//			private String hostedUrl;  public String getHostedUrl(){return hostedUrl;}
			private Map<String,String> info;  public Map<String,String> getInfo(){return info;}
			public Task(String name, String enabled, String status, String health, Map<String,String> info/*, String sourceUrl, String hostedUrl*/){ this.name=name; this.enabled=enabled; this.status=status; this.health=health; this.info=info; /*this.sourceUrl=sourceUrl; this.hostedUrl=hostedUrl;*/ }
		}
		List<Task> result=new ArrayList<Task>();
		
		Database db=Database.get();
		boolean dbUpdated=false;
		for(Map<String, Object> c:Config.get().getList()){
			String name=(String)c.get("name");
			
			// should centralize this method as it's used/copy-pasted elsewhere
    	if (!db.getTasks().containsKey(name)){
    		db.getTasks().put(name, new MapBuilder<String,String>().put("name", name).put("status", "X|999").put("health", String.format("%20s", "").replaceAll(" ", "X")).build());
    		dbUpdated=true;
    	}

			Map<String, String> task=db.getTasks().get(name);
			
			Map<String, String> info=new HashMap<String, String>();
			info.put("Ping URL", (String)c.get("url"));
			info.put("Source URL", (String)c.get("info-sourceUrl"));
			info.put("Hosted URL", (String)c.get("info-hostedUrl"));
			
			result.add(new Task(name, (String)c.get("enabled"), task.get("status"), task.get("health"), info/*, (String)c.get("info-sourceUrl"), (String)c.get("info-hostedUrl")*/));
		}
		
		if (dbUpdated) db.save();
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(result)).build();		
	}
	
	// returns the config file contents
	@GET
	@Path("/config")
	public Response getConfig(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("GetConfig:: Called");
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
	}

	// saves a new complete config
	@POST
	@Path("/config")
	public Response saveConfig(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("SaveConfig:: Called");
		String payload=IOUtils.toString(request.getInputStream());
		System.out.println("payload = "+payload);
		
		Config newConfig=Json.newObjectMapper(true).readValue(payload, Config.class);

		log.debug("SaveConfig:: New Config = " + Json.newObjectMapper(true).writeValueAsString(newConfig));
		newConfig.save();

		// re-start the heartbeat with a new interval
		if (null!=newConfig.getOptions().get("intervalInHours")){
			String heartbeatInterval=newConfig.getOptions().get("intervalInHours");
			log.info("SaveConfig:: Re-setting heartbeat with interval: " + heartbeatInterval+"h");
			Heartbeat.stop();
			Heartbeat.start(TimeUnit.HOURS.toMillis(Long.parseLong(heartbeatInterval)));
		}

		// re-start the ping with a new interval
    long pingIntervalInMs=3600l;
    if (null!=newConfig.getOptions().get("pingIntervalInMinutes")){
    	String pingIntervalInMinutes=newConfig.getOptions().get("pingIntervalInMinutes");
    	pingIntervalInMs=TimeUnit.MINUTES.toMillis(Long.parseLong(pingIntervalInMinutes));
    	log.info("SaveConfig:: Re-setting ping with interval: " + pingIntervalInMinutes +"mins");
    	PingSelf.stop();
    	PingSelf.start(pingIntervalInMs);
    }
    
    // re-start all monitors
    for(Monitor m:monitors.values()) m.stop();
    for(Map<String, Object> t:newConfig.getList()){
    	String name=String.valueOf(t.get("name"));
    	monitors.put(name, Monitor.newInstance(
    			name, 
    			Long.parseLong((String)t.get("pingIntervalInMinutes")), 
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

	@GET
	@Path("/events")
	public Response getEvents() throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("GetEvents:: Called");
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get().getEvents())).build();
	}

	// returns the database content
	@GET
	@Path("/database")
	public Response getDatabase() throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("GetDatabase:: Called");
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get())).build();
	}

	// saves/replaces the database content
	@POST
	@Path("/database")
	public Response saveDatabase(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("SaveDatabase:: Called");
		Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(request.getInputStream()), new TypeReference<Database>(){});
		log.debug("SaveDatabase:: New DB = " + Json.newObjectMapper(true).writeValueAsString(db));
		db.save();
		log.debug("SaveDatabase:: Complete");
		return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database.get())).build();
	}

	@GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFile(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("GetFile:: Called");
		
		String filename=request.getParameter("file");
		
		File file=new File(Config.STORAGE_ROOT, filename);
		
		return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
	      .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
	      .build();
	}

}