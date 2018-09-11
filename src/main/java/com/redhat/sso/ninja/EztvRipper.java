package com.redhat.sso.ninja;

import static com.jayway.restassured.RestAssured.given;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.restassured.response.Response;

public class EztvRipper{

  public static void main(String[] asd){
    new EztvRipper().run();
  }
  
  public void run(){
    
    
    
    String from="S07E01";
    String to="S07E24";
    
    int fromS=Integer.parseInt(from.substring(1,3).replaceAll("S", ""));
    int fromE=Integer.parseInt(from.substring(4,6).replaceAll("E", ""));
    int toS=Integer.parseInt(to.substring(1,3).replaceAll("S", ""));
    int toE=Integer.parseInt(to.substring(4,6).replaceAll("E", ""));
    
    
    System.out.println("From S"+pad(fromS)+"E"+pad(fromE)+" to S"+pad(toS)+"E"+pad(toE));
    
    
    String URL="https://eztv.ag/shows/536/homeland/";
    
    Response response=given().when().get(URL);
    
    String body=response.getBody().asString();
    
    String titleMatches="Homeland.+?%s.+?HDTV.+";
    String exclusions="720p";
    
    for(int s=fromS;s<=toS;s++){
      for(int e=fromE;e<=toE;e++){
        String episode=padS(s)+padE(e);
        //System.out.println("Finding: "+episode);
        
        String pattern="href=\"(magnet.+?"+String.format(titleMatches,episode)+"?\").+?title=\"(.+?)\"";
        
        //System.out.println("Looking for: "+pattern);
        Matcher matcher=Pattern.compile(pattern).matcher(body);
        
        while (matcher.find()){
          String magnet=matcher.group(1);
          String title=matcher.group(2);
          
          if (!magnet.contains(exclusions))
            System.out.println(magnet);
        }
        
      }
    }
    
  }
  
  private String pad(int in){
    return String.format("%2d", in).replaceAll(" ", "0");
  }
  private String padS(int in){
    return String.format("S%2d", in).replaceAll(" ", "0");
  }
  private String padE(int in){
    return String.format("E%2d", in).replaceAll(" ", "0");
  }
}
