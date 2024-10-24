package com.redhat.sso.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;

import com.redhat.sso.backup.Heartbeat;
import com.redhat.sso.backup.MyLoggerFactory;

import io.vertx.ext.web.handler.HttpException;

public class Http{
  private static final Logger log=MyLoggerFactory.getLogger(Http.class);
	public static String get(String url) throws HttpException, UnknownHostException, MalformedURLException, IOException{
    StringBuffer sb=new StringBuffer();
    try{
      HttpURLConnection conn=(HttpURLConnection)new URL(url).openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");
      if (conn.getResponseCode()<200 && conn.getResponseCode()>=300)
        throw new HttpException(conn.getResponseCode(), "Failed : HTTP error code : "+ conn.getResponseCode());
      BufferedReader br=new BufferedReader(new InputStreamReader((conn.getInputStream())));
      String output;
      while ((output=br.readLine())!=null) {
          sb.append(output).append("\n");
      }
      conn.disconnect();
    } catch (UnknownHostException e){
      log.debug("Http.get():: "+e.getClass().getSimpleName()+" thrown by: "+url+". Error="+e.getMessage());
      throw e;
    } catch (MalformedURLException e){
      log.debug("Http.get():: "+e.getClass().getSimpleName()+" thrown by: "+url+". Error="+e.getMessage());
      throw e;
    } catch (IOException e){
      log.debug("Http.get():: "+e.getClass().getSimpleName()+" thrown by: "+url+". Error="+e.getMessage());
      throw e;
    }
    return sb.toString();
  }
	
	public static String post(String url, String data, Map<String,String> headers) throws IOException{
	  return post(url, headers, new MapBuilder<String,String>().build(), data);
	}
	public static String post(String url, Map<String,String> headers, Map<String,String> queryParams, String data) throws IOException{
		HttpURLConnection con=null;
		try{

			URL myurl=new URL(url);
			con=(HttpURLConnection)myurl.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			for(Entry<String, String> e:headers.entrySet()){
				con.setRequestProperty(e.getKey(), e.getValue());	
			}
			
			StringBuffer qp=new StringBuffer();
			for(Entry<String, String> e:queryParams.entrySet()){
				qp.append("&"+e.getKey()+"="+e.getValue());
			}
			if (qp.length()>0){
			  qp.deleteCharAt(0);
			  url+="?"+qp.toString();
			}
			
			if (null!=data){
				try (DataOutputStream wr=new DataOutputStream(con.getOutputStream())){
					wr.write(data.getBytes());
				}
			}

			StringBuilder content;
			try (BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){

				String line;
				content=new StringBuilder();

				while ((line=br.readLine()) != null){
					content.append(line);
					content.append(System.lineSeparator());
				}
			}
			
			System.out.println(content.toString());
			return content.toString();

		}finally{
			if (null!=con)
				con.disconnect();
		}
	}
	
  public static ResponseBuilder newResponse(int status, String contentType, String entity){
  	String nonce="";boolean addNonce=false;
//  	if (addNonce){
//  		nonce=RandomStringUtils.random(10, "ABCDEFGHIJKLMOPQRSTUVWXYZ1234567890");
//	  	if (null!=entity)
//	  		entity=entity.replace("$NONCE", nonce);
//  	}
  	
  	return Response.status(status)
  			.entity(entity)
       .header("Access-Control-Allow-Origin",  "*")
       .header("Content-Type",contentType)
       .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
       .header("Pragma", "no-cache")
//       .header("Content-Security-Policy", "default-src 'self' data: 'unsafe-inline' "+(addNonce?"'nonce-"+nonce+"'":"")+" www.redhat.com http://cdn.datatables.net https://cdn.jsdelivr.net https://cdnjs.cloudflare.com http://bartaz.github.io https://lh3.googleusercontent.com")
       .header("X-Content-Type-Options", "nosniff");
  }
  @Deprecated public static ResponseBuilder newOkHtmlResponse(){ // deprecated because it skips script security header info
  	return newResponse(200, "text/html; charset=UTF-8", null);
  }
  public static ResponseBuilder newOkHtmlResponse(String entity){
  	return newResponse(200, "text/html; charset=UTF-8", entity);
  }
  public static ResponseBuilder newHtmlResponse(int status){
  	return newResponse(status, "text/html; charset=UTF-8", null);
  }
  public static ResponseBuilder newResponse(int status){
  	return newResponse(status, "application/json", null);
  }
}
