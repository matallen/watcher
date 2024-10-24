package com.redhat.sso.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.Resources;


@Path("/")
public class UIController{
	private static final Logger log=LoggerFactory.getLogger(UIController.class);
	public static Map<String, Monitor> monitors=new HashMap<String, Monitor>();
	
	public static void main(String[ ]asd){
	}
  
  @GET @Path("/{page}") public Response servePage(@PathParam("page") String page) throws FileNotFoundException, IOException{
    log.info("/"+page+" Called");
    return Http.newOkHtmlResponse(Resources.getTemplateAsString(page+".html")
        .replace("<!--HEADER_TEMPLATE-->", Resources.getTemplateAsString("header.html"))
        .replace("<!--NAV_TEMPLATE-->", Resources.getTemplateAsString("nav.html"))
        ).build();
  }

}