package com.profiler.data.store.hbase.local2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE_BYTE_BUFFER;

import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.TGet;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TResult;

import com.profiler.data.store.hbase.get2.GetDatabaseData;
import com.profiler.data.store.hbase.get2.GetServerData;
import com.profiler.util.Converter;

public class LocalDataManager {
	public static Hashtable<Integer,String> queryTable=new Hashtable<Integer,String>();
	public static Hashtable<Integer,String> dbTable=new Hashtable<Integer,String>();
	public static Hashtable<Integer,String> agentTable=new Hashtable<Integer,String>();
	public static String getQuery(THBaseService.Client client,int hashCode) {
		if(queryTable.containsKey(hashCode)) return queryTable.get(hashCode);
		else {
			updateQueryTable(client);
			if(queryTable.containsKey(hashCode)) {
				return queryTable.get(hashCode);
			} else {
				return hashCode+" - -";
			}
		}
	}
	public static String getDB(THBaseService.Client client,int hashCode) {
		if(dbTable.containsKey(hashCode)) return dbTable.get(hashCode);
		else {
			updateDBTable(client);
			if(dbTable.containsKey(hashCode)) {
				return dbTable.get(hashCode);
			} else {
				return hashCode+" - -";
			}
		}
	}
	private static void putValue(TResult row,Hashtable<Integer,String> table) {
		int hashCode=Integer.parseInt(new String(row.getRow()));
		
		List<TColumnValue> columnList=row.getColumnValues();
		TColumnValue columnValue=columnList.get(0);
		String queryString=new String(columnValue.getValue());
		
		table.put(hashCode,queryString);
	}
	private static void updateQueryTable(THBaseService.Client client) {
		
		GetDatabaseData getDatabase=new GetDatabaseData();
		try {
			List<TResult> queryList=getDatabase.getQueryList(client);
			System.out.println("queryListSize="+queryList.size());
			for(TResult row:queryList) {
				putValue(row,queryTable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private static void updateDBTable(THBaseService.Client client) {
		GetDatabaseData getDatabase=new GetDatabaseData();
		try {
			List<TResult> dbList=getDatabase.getDBList(client);
			System.out.println("dbListSize="+dbList.size());
			for(TResult row:dbList) {
				putValue(row,dbTable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getAgentName(THBaseService.Client client, int hashCode) {
		if(agentTable.containsKey(hashCode)) return agentTable.get(hashCode);
		else {
			updateAgentTable(client);
			if(agentTable.containsKey(hashCode)) {
				return agentTable.get(hashCode);
			} else {
				return hashCode+"";
			}
		}
		
	}
	public static String getAgentName(THBaseService.Client client, String hashCode) {
		return getAgentName(client,Integer.parseInt(hashCode));
	}
	private static void updateAgentTable(THBaseService.Client client) {
		
		GetServerData getServer=new GetServerData();
		try {
			List<TGet> rowNameList=getServer.getServerList(client);
			List<TResult> serverList=client.getMultiple(HBASE_SERVER_TABLE_BYTE_BUFFER, rowNameList);
			for(TResult result:serverList) {
				Map<String,String> map=Converter.tResultToMap(result, null, true);
				
				String serverHashCode=new String(result.getRow());
				String instanceNameKey=HBASE_SERVER_COLUMN_INSTANCE_NAME;
				ByteBuffer instanceName=Converter.toByteBuffer(instanceNameKey);
				String insertData=null;
				if(map.containsKey(instanceName)) {
					insertData=map.get(HBASE_SERVER_COLUMN_INSTANCE_NAME);
				} else {
					String ipCell=map.get(HBASE_SERVER_COLUMN_IP);
					String portCell=map.get(HBASE_SERVER_COLUMN_PORTS);
					insertData=ipCell+"_"+portCell;
				}
				agentTable.put(Integer.parseInt(serverHashCode), insertData);
			}
			
		} catch (Exception e) {
			
		}
		
	}
	
}
