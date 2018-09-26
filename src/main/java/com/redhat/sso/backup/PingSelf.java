package com.redhat.sso.backup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

//import com.jayway.restassured.response.Response;
import com.redhat.sso.utils.Http;
import com.redhat.sso.utils.Http.Response;

//import static io.restassured.RestAssured.given;
//import static com.jayway.restassured.RestAssured.given;

public class PingSelf{
	private static final Logger log=Logger.getLogger(PingSelf.class);
	private static Timer t;
	private static final Long startupDelay=30000l;

	public static void main(String[] asd){
		try{
			PingSelf.runOnce();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void runOnce(){
		new PingSelfRunnable().run();
	}

	public static void start(long intervalInMs){
		t=new Timer("backup-PingSelf", false);
		t.scheduleAtFixedRate(new PingSelfRunnable(), startupDelay, intervalInMs);
	}

	public static void stop(){
		t.cancel();
	}

	static class PingSelfRunnable extends TimerTask{
		@Override
		public void run(){
			log.info("PingSelf fired");

			String url=System.getenv("PING_URL");
			
			if (url == null || "".equals(url)) {
				log.debug("No \"PING_URL\" system property specified, skipping...");
				return;
			}
			
				Response response=Http.get(url);
				log.debug("Ping: called '" + url + "', response code was: " + response.responseCode);
				if (200 != response.responseCode){
					// notification?
					
////					Properties props=new Properties();
////					props.put("mail.smtp.starttls.enable", "true");
////					props.put("mail.smtp.auth", "true");
////					props.put("mail.smtp.host", "smtp.gmail.com");
////					props.put("mail.smtp.port", "587");
////
////					Session session=Session.getInstance(props);
////					MimeMessage message=new MimeMessage(session);
////					message.setFrom(new InternetAddress("mojo-search-widget@redhat.com"));
////					message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse("mallen@redhat.com"));
////					message.setSubject("Health Check Ping Failure: code=" + responseCode);
////					message.setText("Hit the url \"" + url + "\" and got the response code \"" + responseCode + "\" and payload \"" + response.toString() + "\"");
////					Transport.send(message);
				}
				
		}
	}

}
