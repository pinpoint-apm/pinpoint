package com.profiler.data.store.hbase.get;


import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_TABLE_BYTE_BUFFER;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE_BYTE_BUFFER;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TCell;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;

public class GetDatabaseData extends AbstractGetData {
	public GetDatabaseData() {
		super("Database");
	}
	@Override
	public CharSequence getData() {
		
		return null;
	}
	public List<TRowResult> getDBList(Hbase.Client client) throws Exception{
		List<TRowResult> rowList=client.getRow(HBASE_DATABASE_TABLE_BYTE_BUFFER, HBASE_DATABASE_ROW_DB_ROW_NAMES);
		List<ByteBuffer> dbHashCodeList=new ArrayList<ByteBuffer>();
		for(TRowResult result:rowList) {
			Map<ByteBuffer,TCell> agentMap=result.getColumns();
			Set<ByteBuffer> keySet=agentMap.keySet();
			for(ByteBuffer key:keySet) {
				TCell tempValue=agentMap.get(key);
				ByteBuffer tempData=tempValue.bufferForValue();
				
				dbHashCodeList.add(tempData);
				//For debugging. After debug, it can remove
//				String value=new String(tempData.array());
//				log("DB rowName="+value);
			}
		}
		List<TRowResult> dbList=client.getRows(HBASE_DATABASE_TABLE_BYTE_BUFFER, dbHashCodeList);
		return dbList;
	}
	public List<TRowResult> getQueryList(Hbase.Client client) throws Exception{
		List<TRowResult> rowList=client.getRow(HBASE_DATABASE_TABLE_BYTE_BUFFER, HBASE_DATABASE_ROW_QUERY_ROW_NAMES);
		List<ByteBuffer> queryHashCodeList=new ArrayList<ByteBuffer>();
		for(TRowResult result:rowList) {
			Map<ByteBuffer,TCell> agentMap=result.getColumns();
			Set<ByteBuffer> keySet=agentMap.keySet();
			for(ByteBuffer key:keySet) {
				TCell tempValue=agentMap.get(key);
				ByteBuffer tempData=tempValue.bufferForValue();
				
				queryHashCodeList.add(tempData);
				//For debugging. After debug, it can remove
//				String value=new String(tempData.array());
//				log("Query rowName="+value);
			}
		}
		List<TRowResult> queryList=client.getRows(HBASE_DATABASE_TABLE_BYTE_BUFFER, queryHashCodeList);
		return queryList;
	}
}
