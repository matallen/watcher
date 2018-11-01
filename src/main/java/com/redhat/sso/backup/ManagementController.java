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
	public static List<Monitor> monitors=new ArrayList<Monitor>();
	
	
	public static void main(String[ ]asd){
		System.out.println(String.format("%5s", "").replaceAll(" ", "X"));
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
		
//		System.out.println("looking in : "+taskRootFolder.getAbsolutePath());
//		
//		System.out.println("'"+taskRootFolder.getAbsolutePath()+"'.exists="+taskRootFolder.exists());
		
		class Backup{
			private String name;
			private String file;
			public Backup(String name, String file){
				this.name=name;
				this.file=file;
			}
			public String getName(){return name;}
			public String getFile(){return file;}
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
		
		class Task{
			private String name;
			private String status;
			private String health;
			public Task(String name, String status, String health){
				this.name=name;
				this.status=status;
				this.health=health;
			}
			public String getName(){return name;}
			public String getStatus(){return status;}
			public String getHealth(){return health;}
		}
		List<Task> result=new ArrayList<Task>();
		
		Database db=Database.get();
		boolean dbUpdated=false;
		for(Map<String, Object> t:Config.get().getList()){
			String name=(String)t.get("name");
			if (Monitor.initTaskIfNecessary(db, name)) dbUpdated=true;
			Map<String, String> task=db.getTasks().get(name);
			result.add(new Task(name, task.get("status"), task.get("health")));
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
//		if (null != heartbeatInterval && heartbeatInterval.matches("\\d+")){
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
    
    
    for(Monitor m:monitors)
    	m.stop();
    
    for(Map<String, Object> t:newConfig.getList()){
    	monitors.add(Monitor.newInstance(t.get("name")+"", Long.parseLong((String)t.get("pingIntervalInMinutes")), t.get("url")+""));
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