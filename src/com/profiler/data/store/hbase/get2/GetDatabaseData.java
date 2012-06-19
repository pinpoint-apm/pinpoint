package com.profiler.data.store.hbase.get2;


import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_TABLE_BYTE_BUFFER;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.TGet;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TResult;
import org.apache.thrift.transport.TTransportException;

public class GetDatabaseData extends AbstractGetData {
	public GetDatabaseData() {
		super("Database");
	}
	@Override
	public CharSequence getData() {
		StringBuilder data=new StringBuilder();
		THBaseService.Client client=getClient();
		if(client!=null) {
			try {
				data.append("<H3>DB List</H3>");
				List<TResult> dbList=getDBList(client);
				appendList(data,dbList);

				data.append("<H3>Query List</H3>");
				List<TResult> queryList=getQueryList(client);
				appendList(data,queryList);
			} catch(TTransportException tte) {
				System.err.println(tte.getMessage());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	private void appendList(StringBuilder data,List<TResult> list) {
		data.append("<TABLE border=1>");
		data.append("<TR><TD>hashCode</TD><TD>Data</TD></TR>");
		for(TResult row:list) {
			int hashCode=Integer.parseInt(new String(row.getRow()));
			
			List<TColumnValue> columnList=row.getColumnValues();
			TColumnValue columnValue=columnList.get(0);
			String queryString=new String(columnValue.getValue());
			data.append("<TR><TD>").append(hashCode).append("</TD><TD>").append(queryString).append("</TD></TR>");
		}
		data.append("</TABLE>");
	}
	private List<TResult> getList(THBaseService.Client client,ByteBuffer rowName) throws Exception{
		TGet get=new TGet();
		get.setRow(rowName);
		TResult rowList=client.get(HBASE_DATABASE_TABLE_BYTE_BUFFER, get);
		List<TGet> hashCodeList=new ArrayList<TGet>();
		List<TColumnValue> columnValues=rowList.getColumnValues();
		for(TColumnValue result:columnValues) {
			TGet tempGet=new TGet();
			tempGet.setRow(result.getValue());
			hashCodeList.add(tempGet);
		}
		List<TResult> list=client.getMultiple(HBASE_DATABASE_TABLE_BYTE_BUFFER, hashCodeList);
		return list;
	}
	public List<TResult> getDBList(THBaseService.Client client) throws Exception{
		return getList(client,HBASE_DATABASE_ROW_DB_ROW_NAMES);
	}
	public List<TResult> getQueryList(THBaseService.Client client) throws Exception{
		return getList(client,HBASE_DATABASE_ROW_QUERY_ROW_NAMES);
	}
}
