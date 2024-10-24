package com.redhat.sso.backup;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.HttpOld;
import com.redhat.sso.utils.MapBuilder;

import io.vertx.ext.web.handler.HttpException;

/**
 * Integration with google or slack chat boards, to push notifications of events such as user promotions, script failures etc..
 * @author mallen
 */
public class AlertChat{
	public enum ChatEvent{onHttpFailure,onError}
	
	public void send(ChatEvent type, String name, String taskUrl, String httpResponse){
//	  System.out.println("send alert called");
		Config c=Config.get();
		if ("true".equalsIgnoreCase(Config.get().getOptions().get("notifications.enabled"))){
//		  System.out.println("send alert - notification on");
			for(Map<String, String> notification:c.getNotifications()){
					List<String> events=Arrays.asList(notification.get("events").split(","));
					if (events.contains(type.name())){
						// send the notification!
						String channel=notification.get("channel");
						String template=getTemplate(c, channel);
						String alertPayload=template.replaceAll("TASK_NAME", name).replaceAll("RESPONSE_CODE", httpResponse).replaceAll("TASK_URL", taskUrl);
						System.out.println(String.format("sending this; channel=%s, payload=%s", channel, alertPayload));
						try{
						  String r=Http.post(channel, alertPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
						  System.out.println("Sending '"+alertPayload+"' to google chat api. response.code=200");
						}catch(HttpException e){
						  System.out.println("Sending '"+alertPayload+"' to google chat api but hit an error: response.code="+e.getStatusCode());
						}catch(IOException e){
						  System.out.println("Sending '"+alertPayload+"' to google chat api but hit an error: "+ e.getMessage());
						}
					}
//				}
			}
		}
	}
	
	private String getTemplate(Config c, String channel){
	  if (channel.contains("slack")) return c.getOptions().get("googlechat.webhook.template");
	  if (channel.contains("google") && channel.contains("chat")) return c.getOptions().get("googlechat.webhook.template");
	  return null;
	}
}
