package com.redhat.sso.backup;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;


public class MyLoggerFactory{
	public static Logger getLogger(Class<?> clazz){
		Logger l=new MyLoggerFactory().new Logger(clazz.getSimpleName());
		l.realLogger=LoggerFactory.getLogger(clazz);
		l.logToSysOut="true".equalsIgnoreCase(Config.get().getProperty("LOG_TO_SYSOUT"));
		return l;
	}
	
	public class Logger implements org.slf4j.Logger{
		protected org.slf4j.Logger realLogger;
		private String name;
		private boolean logToSysOut=true;
		public Logger(String name){this.name=name;}
		@Override public String getName(){return name;}
		@Override public boolean isTraceEnabled(){return false;}
		@Override public void trace(String format, Object arg){}
		@Override public void trace(String format, Object arg1, Object arg2){}
		@Override public void trace(String format, Object... arguments){}
		@Override public void trace(String msg, Throwable t){}
		@Override public boolean isTraceEnabled(Marker marker){return false;}
		@Override public void trace(Marker marker, String msg){}
		@Override public void trace(Marker marker, String format, Object arg){}
		@Override public void trace(Marker marker, String format, Object arg1, Object arg2){}
		@Override public void trace(Marker marker, String format, Object... argArray){}
		@Override public void trace(Marker marker, String msg, Throwable t){}
		@Override public boolean isDebugEnabled(){
			return false;
		}
		static final int maxNameLen=13;
		@Override public void debug(String msg){ if (!logToSysOut){ realLogger.debug(msg); }else{ System.out.println(String.format("DEBUG::%-"+maxNameLen+"s::%s",name.length()>=maxNameLen?name.substring(0, maxNameLen):name,msg)); }}
		@Override public void debug(String format, Object arg){}
		@Override public void debug(String format, Object arg1, Object arg2){}
		@Override public void debug(String format, Object... arguments){}
		@Override public void debug(String msg, Throwable t){}
		@Override public boolean isDebugEnabled(Marker marker){ return false; }
		@Override public void debug(Marker marker, String msg){}
		@Override public void debug(Marker marker, String format, Object arg){}
		@Override public void debug(Marker marker, String format, Object arg1, Object arg2){}
		@Override public void debug(Marker marker, String format, Object... arguments){}

		@Override
		public void debug(Marker marker, String msg, Throwable t){}
		@Override
		public boolean isInfoEnabled(){
			return true;
		}

		@Override
		public void info(String msg){
			 if (!logToSysOut){ realLogger.info(msg); }else{ System.out.println("INFO::"+name+"::"+msg); }
		}

		@Override public void info(String format, Object arg){}

		@Override
		public void info(String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isInfoEnabled(Marker marker){
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void info(Marker marker, String msg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(Marker marker, String format, Object arg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(Marker marker, String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(Marker marker, String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void info(Marker marker, String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isWarnEnabled(){
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void warn(String msg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(String format, Object arg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isWarnEnabled(Marker marker){
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void warn(Marker marker, String msg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(Marker marker, String format, Object arg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(Marker marker, String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(Marker marker, String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void warn(Marker marker, String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isErrorEnabled(){
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void error(String msg){
			 if (!logToSysOut){ realLogger.error(msg); }else{ System.out.println("ERROR::"+name+"::"+msg); }

		}

		@Override
		public void error(String format, Object arg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isErrorEnabled(Marker marker){
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void error(Marker marker, String msg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(Marker marker, String format, Object arg){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(Marker marker, String format, Object arg1, Object arg2){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(Marker marker, String format, Object... arguments){
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(Marker marker, String msg, Throwable t){
			// TODO Auto-generated method stub
			
		}
		@Override
		public void trace(String msg){
			// TODO Auto-generated method stub
			
		}
		
	}
}
