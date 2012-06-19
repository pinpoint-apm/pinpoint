package com.profiler.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

public class TomcatProfilerConfig {
	public static String SERVER_IP="127.0.0.1";
//	public static String SERVER_IP="10.25.131.94";
	public static String TOMCAT_LIB_PATH="/home1/irteam/apps/tomcat";
	static {
		String catalinaHome=System.getProperty("catalina.home");
		if(catalinaHome!=null) {
			TOMCAT_LIB_PATH=catalinaHome;
		}
	}

	
	public static int AGENT_TCP_LISTEN_PORT=9990;
	public static int SERVER_TCP_LISTEN_PORT=9991;
	public static int REQUEST_TRANSACTION_DATA_LISTEN_PORT=9995;
	public static int REQUEST_DATA_LISTEN_PORT=9996;
	public static int JVM_DATA_LISTEN_PORT=9997;
	
	
	public static long JVM_STAT_GAP=5000;
	public static long SERVER_CONNECT_RETRY_GAP=1000;
	
	/**
	 * If sql query count is over 10000 it consumes Memory.
	 * So sqlHashSet uses CopyOnWriteArraySet. 
	 * It is slow, but it is stable. 
	 * Default set is false and it uses HashSet.
	 */
	public static boolean QUERY_COUNT_OVER_10000=false;
	public static boolean JDBC_PROFILE=true;
//	public static boolean URL_COUNT_OVER_10000=false;
	
//	public static String MSSQL_ENCODING="MS949";
	
	static {
		readConfigFile();
	}
	public static void readConfigFile() {
		String hippoConfigFileName=System.getProperty("hippo.config");
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
			System.out.println("##### hippo.config property is not set. Using default values #####");
		}
	}
	private static void setPropertyValues(Properties prop) {
		Object temp=null;
		//##### set String values #####
		if((temp=prop.get("SERVER_IP"))!=null) 
			SERVER_IP=temp.toString();
		if((temp=prop.get("TOMCAT_LIB_PATH"))!=null)
			TOMCAT_LIB_PATH=temp.toString();

		//##### set int values #####
		if((temp=prop.get("AGENT_TCP_LISTEN_PORT"))!=null) 
			AGENT_TCP_LISTEN_PORT=Integer.parseInt(temp.toString());
		if((temp=prop.get("SERVER_TCP_LISTEN_PORT"))!=null) 
			SERVER_TCP_LISTEN_PORT=Integer.parseInt(temp.toString());
		if((temp=prop.get("REQUEST_TRANSACTION_DATA_LISTEN_PORT"))!=null) 
			REQUEST_TRANSACTION_DATA_LISTEN_PORT=Integer.parseInt(temp.toString());
		if((temp=prop.get("REQUEST_DATA_LISTEN_PORT"))!=null) 
			REQUEST_DATA_LISTEN_PORT=Integer.parseInt(temp.toString());
		if((temp=prop.get("JVM_DATA_LISTEN_PORT"))!=null) 
			JVM_DATA_LISTEN_PORT=Integer.parseInt(temp.toString());
		
		//##### set long values #####
		if((temp=prop.get("JVM_STAT_GAP"))!=null) 
			JVM_STAT_GAP=Long.parseLong(temp.toString());
		if((temp=prop.get("SERVER_CONNECT_RETRY_GAP"))!=null) 
			SERVER_CONNECT_RETRY_GAP=Long.parseLong(temp.toString());
		
		//##### set boolean values #####
		if((temp=prop.get("QUERY_COUNT_OVER_10000"))!=null) 
			QUERY_COUNT_OVER_10000=Boolean.parseBoolean(temp.toString());
		if((temp=prop.get("JDBC_PROFILE"))!=null) 
			JDBC_PROFILE=Boolean.parseBoolean(temp.toString());
		System.out.println("##### Hippo Config Loaded successfully. #####");
	}
}
