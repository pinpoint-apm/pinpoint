package com.profiler.data.store.hbase.local;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE_BYTE_BUFFER;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;
import static com.profiler.config.TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_GET_CONNECTION;
import static com.profiler.config.TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_QUERY;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TCell;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;

import com.profiler.data.store.hbase.get.GetDatabaseData;
import com.profiler.data.store.hbase.get.GetServerData;
import com.profiler.util.Converter;

public class LocalDataManager {
	public static Hashtable<Integer,String> queryTable=new Hashtable<Integer,String>();
	public static Hashtable<Integer,String> dbTable=new Hashtable<Integer,String>();
	public static Hashtable<Integer,String> agentTable=new Hashtable<Integer,String>();
	public static String getQuery(Hbase.Client client,int hashCode) {
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
	public static String getDB(Hbase.Client client,int hashCode) {
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
	private static void putValue(TRowResult row,String key,Hashtable<Integer,String> table) {
		Map<ByteBuffer,TCell> map=row.getColumns();
		String hashCode=new String(row.getRow());
		ByteBuffer value=Converter.toByteBuffer(key);
		TCell cell=map.get(value);
		table.put(Integer.parseInt(hashCode), new String(cell.getValue()));
	}
	private static void updateQueryTable(Hbase.Client client) {
		GetDatabaseData getDatabase=new GetDatabaseData();
		try {
			List<TRowResult> queryList=getDatabase.getQueryList(client);
			System.out.println("queryListSize="+queryList.size());
			for(TRowResult row:queryList) {
				String key=HBASE_DATABASE_COLUMN_QUERY_STRING+REQ_DATA_TYPE_DB_QUERY;
				putValue(row,key,queryTable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void updateDBTable(Hbase.Client client) {
		GetDatabaseData getDatabase=new GetDatabaseData();
		try {
			List<TRowResult> dbList=getDatabase.getDBList(client);
			System.out.println("queryListSize="+dbList.size());
			for(TRowResult row:dbList) {
				String key=HBASE_DATABASE_COLUMN_CONNECTION_URL+REQ_DATA_TYPE_DB_GET_CONNECTION;
				putValue(row,key,dbTable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getAgentName(Hbase.Client client, int hashCode) {
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
	public static String getAgentName(Hbase.Client client, String hashCode) {
		return getAgentName(client,Integer.parseInt(hashCode));
	}
	private static void updateAgentTable(Hbase.Client client) {
		GetServerData getServer=new GetServerData();
		try {
			List<ByteBuffer> rowNameList=getServer.getServerList(client);
			List<TRowResult> serverList=client.getRows(HBASE_SERVER_TABLE_BYTE_BUFFER, rowNameList);
			for(TRowResult row:serverList) {
				Map<ByteBuffer,TCell> map=row.getColumns();
				String serverHashCode=new String(row.getRow());
				String instanceNameKey=HBASE_SERVER_COLUMN_INSTANCE_NAME+serverHashCode;
				ByteBuffer instanceName=Converter.toByteBuffer(instanceNameKey);
				String insertData=null;
				if(map.containsKey(instanceName)) {
					TCell cell=map.get(instanceName);
					insertData=new String(cell.getValue());
				} else {
					String ipKey=HBASE_SERVER_COLUMN_IP+serverHashCode;
					String portsKey=HBASE_SERVER_COLUMN_PORTS+serverHashCode;
					TCell ipCell=map.get(Converter.toByteBuffer(ipKey));
					TCell portCell=map.get(Converter.toByteBuffer(portsKey));
					insertData=new String(ipCell.getValue())+"_"+new String(portCell.getValue());
				}
				agentTable.put(Integer.parseInt(serverHashCode), insertData);
			}
			
		} catch (Exception e) {
			
		}
	}
	
}
