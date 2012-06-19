package com.profiler.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

public class TomcatProfilerReceiverConfig {
	public static String HBASE_THRIFT_IP="127.0.0.1";
	public static int HBASE_THRIFT_PORT=9092;
	
	
	static {
		readConfigFile();
	}
	public static void readConfigFile() {
		String configFileName="hipposerver.config";
		String hippoConfigFileName=System.getProperty(configFileName);
		if(hippoConfigFileName!=null) {
//			System.out.println("%%%%%%%%%%%%%%%%%%%%% hippo.config File="+hippoConfigFileName);
			Properties prop=new Properties();
			try {
				FileReader reader=new FileReader(hippoConfigFileName);
				prop.load(reader);
				reader.close();
				setPropertyValues(prop);
			} catch(FileNotFoundException fnfe) {
				System.out.println("##### "+hippoConfigFileName+" file is not exists. Please check configuration.");
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("##### "+configFileName+" property is not set. Using default values #####");
		}
	}
	private static void setPropertyValues(Properties prop) {
		Object temp=null;
		//##### set String values #####
		if((temp=prop.get("HBASE_THRIFT_IP"))!=null) 
			HBASE_THRIFT_IP=temp.toString();
		//##### set int values #####
		if((temp=prop.get("HBASE_THRIFT_PORT"))!=null) 
			HBASE_THRIFT_PORT=Integer.parseInt(temp.toString());
		
		//##### set boolean values #####
		System.out.println("##### Hippo Config Loaded successfully. #####");
	}
}
