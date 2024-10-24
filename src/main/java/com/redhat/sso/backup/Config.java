package com.redhat.sso.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.ConfigProvider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Maps;
import com.redhat.sso.utils.Json;

public class Config{
	private static Config _instance;
	private Map<String,String> options=null;
	private List<Map<String,String>> notifications=null;
	public Map<String,String> getOptions() {if (options==null) options=new HashMap<String, String>(); return options;}
	public List<Map<String,String>> getNotifications() {if (notifications==null) notifications=new ArrayList<Map<String, String>>(); return notifications;}
	public static boolean verboseDebug=false;
	public static File STORAGE;//new File("target/persistence", "config.json");
	public static File STORAGE_ROOT;
//	private long lastModified=-1;
	public static final int DEBUG_SAMPLE_SIZE=2;
	
	private Map<String,Map<String,Object>> plugins;
	public Map<String,Map<String,Object>> getPlugins() {if (plugins==null) plugins=Maps.newLinkedHashMap(); return plugins;}
	private List<Map<String,Object>> list=null;
	public List<Map<String,Object>> getList() {if (list==null) list=new ArrayList<Map<String, Object>>(); return list;}
	
	public Config(){
		STORAGE=new File(Paths.get("target").toFile().exists()?"target/persistence":"persistence", "config.json");
		STORAGE_ROOT=STORAGE.getParentFile();
	}
	public static Config get(){
		_instance=null;
		if (null==_instance)
			_instance=new Config().load();
		return _instance;
	}
	public void save() throws FileNotFoundException, IOException{
		String data=Json.toJson(this);
		STORAGE.getParentFile().mkdirs();
//		System.out.println("saving to "+STORAGE.getAbsolutePath()+":\n"+data);
		IOUtils.write(data, new FileOutputStream(STORAGE), "UTF-8");
		_instance=null; // clear cached config so when it's saved it gets reloaded
	}
	private Config load(){
		try{
//		  System.out.println("loading from : "+STORAGE.getAbsolutePath());
			return Json.toObject(new FileInputStream(STORAGE), Config.class);
		}catch (JsonParseException e){
		}catch (JsonMappingException e){
		}catch (IOException e){
		}
		try{
			Config config=Json.toObject(Config.class.getClassLoader().getResourceAsStream("config.json"), Config.class); // load defaults
			config.save();
			return config;
		}catch (IOException e){}
		return null;
	}
	
	public String getPropertySilent(String... nameDefault){ // using varargs as optional param logic for a default value
		boolean save=verboseDebug;
		verboseDebug=false;
		String result=getProperty(nameDefault);
		verboseDebug=save;
		return result;
	}
	
	private static Map<String, Object> block=Maps.newHashMap();
	private static Map<String,String> propertyCache=Maps.newHashMap();
	private static Map<String,Long> propertyCacheTimeout=Maps.newHashMap();
//	private static long timeout=0; 
	private static long MAX_IN_MS=30000; // 30 seconds
	
	public String getProperty(String... nameDefault){
		if (!propertyCache.containsKey(nameDefault[0]) || (propertyCacheTimeout.containsKey(nameDefault[0]) && propertyCacheTimeout.get(nameDefault[0])<=System.currentTimeMillis())){
//			if (!block.containsKey(nameDefault[0])) block.put(nameDefault[0], new Object());
//			synchronized(nameDefault[0]){
				String v=getUncachedProperty(nameDefault);
	//			if (v!=null){
					propertyCache.put(nameDefault[0], v);
					propertyCacheTimeout.put(nameDefault[0], System.currentTimeMillis()+MAX_IN_MS);
	//			}
//			}
		}
		return propertyCache.get(nameDefault[0]);
	}
	
	public String getUncachedProperty(String... nameDefault){ // using varargs as optional param logic for a default value
  	String name=nameDefault[0];
  	
  	String d3fault=nameDefault.length>1?nameDefault[1]:null;
  	org.eclipse.microprofile.config.Config microprofileConfig=ConfigProvider.getConfig(); // support for quarkus application.properties file
//  	System.out.println("Looking for property '"+name+"', or env property '"+name.replaceAll("\\.", "_")+"'");
  	if (null!=System.getProperty(name)){
  		if (verboseDebug) System.out.println("Found system property '"+name+"', value = '"+System.getProperty(name)+"'");
  		return System.getProperty(name);
  	}else if (null!=System.getenv(name)){
  		if (verboseDebug) System.out.println("Found env property '"+name+"', value = '"+System.getenv(name)+"'");
  		return System.getenv(name);
  	}else if (options.containsKey(name)){
  		if (verboseDebug) System.out.println("Found options property '"+name+"', value = '"+options.get(name)+"'");
  		return options.get(name);
  	}else if (null!=System.getenv(name.replaceAll("\\.", "_"))){
  		if (verboseDebug) System.out.println("Found modified env property '"+name.replaceAll("\\.", "_")+"', value = '"+System.getenv(name.replaceAll("\\.", "_"))+"'");
  		return System.getenv(name.replaceAll("\\.", "_"));
  	}else if (microprofileConfig.getOptionalValue(name, String.class).isPresent()){
  		return microprofileConfig.getValue(name, String.class);
  	}else{
  		System.out.println("Unable to find '"+name+"' in config, system properties or environment variables, returning default '"+d3fault+"'");
  		return d3fault;
  	}
  }

}
