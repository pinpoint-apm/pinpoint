package com.profiler.logger;

import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ProfilerLogManager {
	private static Hashtable<String,Logger> loggerTable=new Hashtable<String,Logger>();
	public ProfilerLogManager() {}
	//-Djava.util.logging.config.file=/develop/eclipse_work_j2ee/TomcatProfilerReceiver/logging.properties
	public static Logger getLogger(String className) {
//		if(loggerTable.containsKey(className)) {
//			return loggerTable.get(className);
//		} else {
//			return initLogger(className);
//		}
		System.out.println(className);
		Logger logger=Logger.getLogger(className);
		if(logger.getHandlers().length==1) {
			String fileName=getLogFileName(className);
			try {
				FileHandler handler=new FileHandler(fileName,50000,5);
				handler.setFormatter(new SimpleFormatter());
				logger.addHandler(handler);
			} catch(Exception e) {
				e.printStackTrace();
			}
			logger.setLevel(Level.INFO);
		}
		System.out.println(logger.getHandlers().length);
//		LogManager manager=LogManager.getLogManager();
//		Properties prop=System.getProperties();
//		System.out.println(prop);
		return logger;
	}
	
	private static String getLogFileName(String className) {
		if(className.equals("com.profiler.data.read.ReadJVMData")) {
			return "./log/jvm.log";
		}
		return "./log/main.log";
	}
}
