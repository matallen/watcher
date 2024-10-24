package com.redhat.sso.backup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.MapBuilder;

/**
 * Integration with google chat boards, to push notifications of events such as user promotions, script failures etc..
 * @author mallen
 */
public class AlertGoogleChat{
	public enum ChatEvent{onHttpFailure,onError}
	
	public void send(ChatEvent type, String name, String taskUrl, String httpResponse){
//		Config c=Config.get();
//		if ("true".equalsIgnoreCase(Config.get().getOptions().get("notifications.enabled"))){
//			for(Map<String, String> notification:c.getNotifications()){
////				if (!"false".equalsIgnoreCase(notification.get("enabled"))){
//					List<String> events=Arrays.asList(notification.get("events").split(","));
//					if (events.contains(type.name())){
//						// send the notification!
//						String channel=notification.get("channel");
//						String template= c.getOptions().get("googlehangoutschat.webhook.template");
////						String googleHangoutsChatPayload=String.format(template, notificationTexts);
//						String googleHangoutsChatPayload=template.replaceAll("TASK_NAME", name).replaceAll("RESPONSE_CODE", httpResponse).replaceAll("TASK_URL", taskUrl);
//						Response r=Http.post(channel, googleHangoutsChatPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
//						System.out.println("Sending '"+googleHangoutsChatPayload+"' to google chat api. response.code="+r.responseCode);
//					}
////				}
//			}
//		}
	}
}
