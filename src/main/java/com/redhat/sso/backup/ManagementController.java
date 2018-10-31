package com.redhat.sso.backup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

@Path("/")
public class ManagementController{
	private static final Logger log=Logger.getLogger(ManagementController.class);
	private static List<Monitor> monitors=new ArrayList<Monitor>();
	
	
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

		// re-start the heartbeat with a new interval
    long pingIntervalInMs=3600l;
    if (null!=newConfig.getOptions().get("pingIntervalInMinutes")){
    	String pingIntervalInMinutes=newConfig.getOptions().get("pingIntervalInMinutes");
    	pingIntervalInMs=TimeUnit.MINUTES.toMillis(Long.parseLong(pingIntervalInMinutes));
    	log.info("SaveConfig:: Re-setting ping with interval: " + pingIntervalInMinutes +"mins");
    	PingSelf.stop();
    	PingSelf.start(pingIntervalInMs);
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