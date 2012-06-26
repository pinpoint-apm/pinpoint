package com.profiler.data.store.hbase.put;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_CONNECTION_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_COLUMN_QUERY_STRING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_DB_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_ROW_QUERY_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_GET_CONNECTION;
import static com.profiler.config.TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_QUERY;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.IOError;
import org.apache.hadoop.hbase.thrift.generated.Mutation;

import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestDataThriftDTO;
public class PutDatabaseData extends AbstractPutData{
	List<RequestDataListThriftDTO> responseDataListDTOList=null;
	public PutDatabaseData(String tableName,List<RequestDataListThriftDTO> responseDataListDTOList) {
		super(tableName);
		this.responseDataListDTOList=responseDataListDTOList;
	}
	public void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) {
		try {
			for(RequestDataListThriftDTO dto:responseDataListDTOList) {
				//If there is no request data, skip saving. 
				if(dto!=null) {
					List<RequestDataThriftDTO> tempList=dto.getRequestDataList();
				
					for(RequestDataThriftDTO tempDto:tempList) {
						int dataType=tempDto.getDataType();
						if(dataType==REQ_DATA_TYPE_DB_GET_CONNECTION) {
							String dataString=tempDto.getDataString();
							if(dataString!=null) {
								int hashCode=tempDto.getDataHashCode();
//								System.out.println("dataString="+dataString);
								saveConnectionInfo(tableNameBuffer,client,hashCode,dataString);
							}
						} else if(dataType==REQ_DATA_TYPE_DB_QUERY) {
							//
							String dataString=tempDto.getDataString();
							if(dataString!=null) {
								int hashCode=tempDto.getDataHashCode();
//								System.out.println("dataString="+dataString);
								saveQuery(tableNameBuffer,client,hashCode,dataString);
							}
						}
					}
				}
			}
		} catch(IOError ioe) {
			logger.error("DatabaseData "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveConnectionInfo(ByteBuffer tableNameBuffer,Hbase.Client client,int hashCode,String url) throws Exception {
		ByteBuffer row=ByteBuffer.wrap((hashCode+"").getBytes());
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(getMutation(HBASE_DATABASE_COLUMN_CONNECTION_URL+REQ_DATA_TYPE_DB_GET_CONNECTION,url.getBytes()));
		client.mutateRow(tableNameBuffer,row,mutations);
//		System.out.println(hashCode);
		
		List<Mutation> rowNamesMutation=new ArrayList<Mutation>();
		rowNamesMutation.add(getMutation(HBASE_DATABASE_COLUMN_DB_ROW_NAMES+hashCode,(hashCode+"").getBytes()));
		client.mutateRow(tableNameBuffer, HBASE_DATABASE_ROW_DB_ROW_NAMES, rowNamesMutation);
//		System.out.println(hashCode);
	}
	private void saveQuery(ByteBuffer tableNameBuffer,Hbase.Client client,int hashCode,String query) throws Exception{
		ByteBuffer row=ByteBuffer.wrap((hashCode+"").getBytes());
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(getMutation(HBASE_DATABASE_COLUMN_QUERY_STRING+REQ_DATA_TYPE_DB_QUERY,query.getBytes()));
		client.mutateRow(tableNameBuffer,row,mutations);
//		System.out.println(hashCode);
		
		List<Mutation> rowNamesMutation=new ArrayList<Mutation>();
		rowNamesMutation.add(getMutation(HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES+hashCode,(hashCode+"").getBytes()));
		client.mutateRow(tableNameBuffer, HBASE_DATABASE_ROW_QUERY_ROW_NAMES, rowNamesMutation);
//		System.out.println(hashCode);
	}
}
