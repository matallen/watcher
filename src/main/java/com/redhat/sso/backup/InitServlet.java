package com.redhat.sso.backup;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class InitServlet extends HttpServlet {
	
  public static void main(String[] asd) throws ServletException{
    new InitServlet().init(null);
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    Heartbeat.start(TimeUnit.HOURS.toMillis(Long.parseLong(Config.get().getOptions().get("intervalInHours"))));
    PingSelf.start(TimeUnit.HOURS.toMillis(Long.parseLong(Config.get().getOptions().get("pingIntervalInHours"))));
  }

  @Override
  public void destroy() {
    super.destroy();
    Heartbeat.stop();
    PingSelf.stop();
  }

}