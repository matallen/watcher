package com.redhat.sso.backup;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Cleanup{
	private static final Logger log = Logger.getLogger(Backup.class);
	static SimpleDateFormat sdf=Backup.sdf;
	static final String DEFAULT_AUTO_DELETE="3 months";
	
	public static void main(String[] asd) throws Exception{
		String filename="assessments.redhat-2022-08-31T18:16:00.bak";
		String pattern="(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})";
//		System.out.println(new Cleanup().extract(filename, pattern));
		Cleanup c=new Cleanup();
		c.run(null);
//		System.out.println(
//				new Cleanup.RegExHelper().apply(pattern).to(filename).asDate(sdf)
//		);
//		
//		LocalDate threshold=c.applyAutoDeleteCfgToTodaysDate(Config.get().getOptions().get("auto.delete.older.than"));
	}
	
	
	private boolean isProperty(Map<String, Object> destination, String property, String value){
		return null!=destination && null!=destination.get(property) && value.equalsIgnoreCase((String)destination.get(property));
	}
	private boolean isEnabled(Map<String, Object> destination){ return isProperty(destination, "enabled", "true"); }
	private boolean isBackupEnabled(Map<String, Object> destination){ return isProperty(destination, "backup", "true"); }
	
	public void run(String taskToBackup) {
		log.info(Cleanup.class.getSimpleName()+ " fired");
    
		try{
//	    Database db=Database.get();
	    String fileDatestampRegex="(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})";
	    String cfg=Config.get().getOptions().get("auto.delete.older.than");
	    if (cfg==null) cfg=DEFAULT_AUTO_DELETE;
			LocalDate threshold=applyAutoDeleteCfgToTodaysDate(cfg);
	    
	    Config.STORAGE_ROOT.mkdirs();
	    
	    for(Map<String, Object> destination:Config.get().getList()){
	    	
	    	if (null==taskToBackup || destination.get("name").equals(taskToBackup)){
	    		if (isEnabled(destination) && isBackupEnabled(destination)){ // should start deleting if we're not backing up, or if the monitoring is disabled
	    			
	    			log.info("Cleaning up old files for: "+destination.get("name"));
		    		
		    		File parentDir=new File(Config.STORAGE_ROOT, (String)destination.get("name"));
		    		
		    		File[] files=parentDir.listFiles();
		    		Arrays.sort(files, new Comparator<File>(){
		    	    public int compare(File f1, File f2){
		    	        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    	    }});
		    		
		    		for (File file:files){
		    			
		    			String fileDatestamp=new RegExHelper().apply(fileDatestampRegex).to(file.getName()).asString();
		    			LocalDate fileLocalDate=sdf.parse(fileDatestamp).toInstant()
		    		      .atZone(ZoneId.systemDefault())
		    		      .toLocalDate();
		    			
		    			if (fileLocalDate.isBefore(threshold)){
		    				// delete it!
//		    				System.out.println("DELETING: "+file.getAbsolutePath());
		    				log.debug("DELETING: "+file.getAbsolutePath());
		    				file.delete();
		    			}else{
//		    				System.out.println("LEAVING : "+file.getAbsolutePath());
		    			}
		    		}
		    		
	    		}
	    		
	    	}
	    }
		}catch(Exception e){
			e.printStackTrace();
		}
    
	}
	
	
	// UTILITY METHODS //
	
	private LocalDate applyAutoDeleteCfgToTodaysDate(String autoDeleteCfg){
//		LocalDate todaysDate=LocalDate.parse(
//				new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()))
//				,DateTimeFormatter.ofPattern("yyyy-MM-dd")
//				);
		LocalDate todaysDate=LocalDate.now();
		ChronoUnit unit=null;
		if (autoDeleteCfg.toLowerCase().contains("min")) unit=ChronoUnit.MINUTES;
		if (autoDeleteCfg.toLowerCase().contains("day")) unit=ChronoUnit.DAYS;
		if (autoDeleteCfg.toLowerCase().contains("week")) unit=ChronoUnit.WEEKS;
		if (autoDeleteCfg.toLowerCase().contains("month")) unit=ChronoUnit.MONTHS;
		int quantity=new RegExHelper().apply("\\d+").to(autoDeleteCfg).asInteger();
		LocalDate threshold=todaysDate.minus(quantity, unit);
		return threshold;
	}
	
	private static class RegExHelper{
		private String r;
		private Pattern p;
		private Map<String,Pattern> cache=new HashMap<String,Pattern>();
		public RegExHelper apply(String pattern){
			if (!cache.containsKey(pattern)) cache.put(pattern, Pattern.compile(pattern));
			p=cache.get(pattern);
			return this;
		}
		public RegExHelper to(String input){
			Matcher m=p.matcher(input);
			if (m.find()) r=m.group();
			return this;
		}
		public Integer asInteger(){ return Integer.parseInt(r); }
		public String asString(){ return r; }
		public Date asDate(SimpleDateFormat sdf) throws ParseException{
			return sdf.parse(r);
		}
	}
}
