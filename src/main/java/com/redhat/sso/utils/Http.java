package com.redhat.sso.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http{
	
	public static class Response{
		public Response(int responseCode, String response){
			this.responseCode=responseCode;
			this.response=response;
		}
		public int responseCode;
		public String response;
	}
	
	public static Response get(String url){
		return http("GET", url, null);
	}
	
	public static Response post(String url, String data){
		return http("POST", url, data);
	}
	
	
	public static void main(String[] asd){
//		Response response=Http.http("GET", "http://openjdk-app-solutiontracker.6923.rh-us-east-1.openshiftapps.com/solution-tracker/versionx", null);
//		
//		Response response2=Http.http("POST", "https://hooks.slack.com/services/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", "payload={\"username\": \"webhookbot\", \"text\": \"SERVER_NAME is DOWN. Failed URL was SERVER_URL.\", \"icon_emoji\": \":ghost:\"}");
		
		Response response=Http.get("https://pathfinder-ui-pathfinder-prod.int.open.paas.redhat.com/pathfinder-ui/");
		
		System.out.println(response.responseCode);
	}
	
	public static synchronized Response http(String method, String url, String data){
		try {
			URL obj=new URL(url);
			HttpURLConnection cnn=(HttpURLConnection)obj.openConnection();
			cnn.setRequestMethod(method.toUpperCase());
			
			if ("POST".equalsIgnoreCase(method) && null!=data){
				cnn.setDoOutput(true);
				OutputStream os = cnn.getOutputStream();
        os.write(data.getBytes());
        os.flush();
			}
			
			Response response=buildResponse(cnn);
			cnn.disconnect();
			return response;
		}catch(IOException e) {
//			return new Response(999, null);
			System.err.println("Http library mis-handled the http response most likely - see exception message: "+ e.getMessage());
			e.printStackTrace();
			return new Response(504, "Connection Timeout");
//			throw new RuntimeException("Http library mis-handled the http response most likely - see exception", e);
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
}
