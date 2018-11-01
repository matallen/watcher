package com.redhat.sso.backup;

import com.redhat.sso.utils.Http;

public class Alert{
	public void alert(String taskName, String taskUrl, int responseCode){
		if ("true".equalsIgnoreCase(Config.get().getOptions().get("slack.webhook.notifications"))){
			String slackTemplate=Config.get().getOptions().get("slack.webhook.template");
			String slackUrl=Config.get().getOptions().get("slack.webhook.url");
			Http.post(slackUrl, slackTemplate.replaceAll("SERVER_NAME", taskName).replaceAll("SERVER_URL", taskUrl));
		}
	}
}
