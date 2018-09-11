package com.redhat.sso.ninja;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.redhat.sso.utils.Json;


@Path("/")
public class ManagementController {
  private static final Logger log=Logger.getLogger(ManagementController.class);
  
  
  // returns the config file contents (and yes, I shouldnt put the http method in the url, but that's a fix for later)
  @GET
  @Path("/config/get")
  public Response configGet(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  
  // saves a new complete config
  @POST
  @Path("/config/save")
  public Response configSave(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    log.info("Saving config");
    Config newConfig=Json.newObjectMapper(true).readValue(request.getInputStream(), Config.class);
    
    log.debug("New Config = "+Json.newObjectMapper(true).writeValueAsString(newConfig));
    newConfig.save();
    
    // re-start the heartbeat with a new interval
    //TODO: reset the heartbeat ONLY if the interval changed from what it was before
    String heartbeatInterval=newConfig.getOptions().get("heartbeat.intervalInSeconds");
    if (null!=heartbeatInterval && heartbeatInterval.matches("\\d+")){
      log.info("Re-setting heartbeat with interval: "+heartbeatInterval);
      Heartbeat2.stop();
      Heartbeat2.start(Long.parseLong(heartbeatInterval));
    }
    
    String maxEvents=newConfig.getOptions().get("maxEvents");
    if (null!=maxEvents && maxEvents.matches("\\d+")){
      Database2.MAX_EVENT_ENTRIES=Integer.parseInt(maxEvents);
    }
    
    log.debug("Saved");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  
  @GET
  @Path("/events")
  public Response getEvents() throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(db.getEvents())).build();
  }
  
}