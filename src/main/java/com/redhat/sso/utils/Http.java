package com.redhat.sso.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		return http("GET", url);
	}
	
	public static Response http(String method, String url){
		try {
			URL obj=new URL(url);
			HttpURLConnection cnn=(HttpURLConnection)obj.openConnection();
			cnn.setRequestMethod(method.toUpperCase());
			Response response=buildResponse(cnn);
			return response;
		}catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Response buildResponse(HttpURLConnection cnn) throws IOException{
		int responseCode=cnn.getResponseCode();
		StringBuffer response=new StringBuffer();
		if (200 != responseCode){
			BufferedReader in=new BufferedReader(new InputStreamReader(cnn.getInputStream()));
			String inputLine;

			while ((inputLine=in.readLine()) != null){
				response.append(inputLine);
			}
			in.close();
		}
		return new Response(responseCode, response.toString());
	}
}
