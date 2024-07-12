package com.redhat.sso.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;


public class Http{
	private static final Logger log = Logger.getLogger(Http.class);
	
	public static class Response{
		public Response(int responseCode, String response){
			this.responseCode=responseCode;
			this.response=response;
		}
		public int responseCode;
		public String response;
		public int getResponseCode(){
			return responseCode;
		}
		public String getString(){
			return response;
		}
	}
	
	public static Response get(String url){
		return http("GET", url, null, null);
	}
	public static Response get(String url, Map<String,String> headers){
		return http("GET", url, null, headers);
	}
	
	public static Response post(String url, String data){
		return http("POST", url, data, null);
	}
	public static Response post(String url, String data, Map<String,String> headers){
		return http("POST", url, data, headers);
	}
	
	public static synchronized Response http(String method, String url, String data, Map<String,String> headers){
		try {
//			log.info("Http call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":""));
			URL obj=new URL(url);
			
			if (url.toLowerCase().contains("https")) ignoreCertErrors(); // ignoring, not good practice but dont want to add all destination apps certs to truststore. opening us up to MitM attack but...
			
			HttpURLConnection cnn=(HttpURLConnection)obj.openConnection();
			cnn.setRequestMethod(method.toUpperCase());
			
			if (headers!=null){
				for(Entry<String, String> e:headers.entrySet()){
					cnn.setRequestProperty(e.getKey(), e.getValue());
				}
			}
			
			if ("POST".equalsIgnoreCase(method) && null!=data){
				cnn.setDoOutput(true);
				OutputStream os = cnn.getOutputStream();
        os.write(data.getBytes());
        os.flush();
			}
			
			Response response=buildResponse(cnn);
//			log.info("Http call responded with code: "+response.responseCode);
			
			
			log.info("Http call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":"")+" - ResponseCode: "+response.responseCode);
			
			cnn.disconnect();
			return response;
		}catch(UnknownHostException e){
			log.error("UnknownHostException:: "+ e.getMessage());
			return new Response(404, e.getMessage());
		}catch(IOException e) {
			log.error("Failure to make call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":""));
			log.error("Http library mis-handled the http response most likely - see exception message: "+ e.getMessage());
			e.printStackTrace();
			return new Response(504, "Connection Timeout");
		}
	}
	
	private static Response buildResponse(HttpURLConnection cnn) throws IOException{
		int responseCode=cnn.getResponseCode();
		StringBuffer response=new StringBuffer();
		if (200 == responseCode){
			BufferedReader in=new BufferedReader(new InputStreamReader(cnn.getInputStream()));
			String inputLine;
			while ((inputLine=in.readLine()) != null)
				response.append(inputLine);
			in.close();
		}
		return new Response(responseCode, response.toString());
	}
	
	public static void ignoreCertErrors(){
	  TrustManager[] trustAllCerts = new TrustManager[]{
	    new X509TrustManager(){
	      public X509Certificate[] getAcceptedIssuers(){ return null; }
	      public void checkClientTrusted(X509Certificate[] certs, String authType) {}
	      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
	    }
	  };

	  try {
	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(null, trustAllCerts, new SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	  } catch (Exception e){
	    e.printStackTrace();
	  }
	}
}
