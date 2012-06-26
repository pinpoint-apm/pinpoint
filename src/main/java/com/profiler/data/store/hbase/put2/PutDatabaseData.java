package com.profiler.data.store.hbase.put2;

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

import org.apache.hadoop.hbase.thrift2.generated.THBaseService.Client;
import org.apache.hadoop.hbase.thrift2.generated.TIOError;
import org.apache.hadoop.hbase.thrift2.generated.TPut;

import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestDataThriftDTO;
public class PutDatabaseData extends AbstractPutData{
	List<RequestDataListThriftDTO> responseDataListDTOList=null;
	public PutDatabaseData(String tableName,List<RequestDataListThriftDTO> responseDataListDTOList) {
		super(tableName);
		this.responseDataListDTOList=responseDataListDTOList;
	}
	/*
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
	*/
	private void saveConnectionInfo(ByteBuffer tableNameBuffer,Client client,int hashCode,String url) throws Exception {
		String hashCodeString=hashCode+"";
		ByteBuffer row=ByteBuffer.wrap((hashCodeString).getBytes());
		
		TPut connPut=new TPut();
		connPut.addToColumnValues(getTColumnValue(HBASE_DATABASE_COLUMN_CONNECTION_URL,REQ_DATA_TYPE_DB_GET_CONNECTION+"",url));
		connPut.setRow(row);
		
		TPut rowNamesPut=new TPut();
		rowNamesPut.addToColumnValues(getTColumnValue(HBASE_DATABASE_COLUMN_DB_ROW_NAMES,hashCodeString,hashCodeString));
		rowNamesPut.setRow(HBASE_DATABASE_ROW_DB_ROW_NAMES);
		
		List<TPut> list=new ArrayList<TPut>();
		list.add(connPut);
		list.add(rowNamesPut);
		client.putMultiple(tableNameBuffer, list);
	}
	private void saveQuery(ByteBuffer tableNameBuffer,Client client,int hashCode,String query) throws Exception{
		String hashCodeString=hashCode+"";
		ByteBuffer row=ByteBuffer.wrap((hashCodeString).getBytes());
		
		TPut queryPut=new TPut();
		queryPut.addToColumnValues(getTColumnValue(HBASE_DATABASE_COLUMN_QUERY_STRING,REQ_DATA_TYPE_DB_QUERY+"",query));
		queryPut.setRow(row);
		
		TPut rowNamesPut=new TPut();
		rowNamesPut.addToColumnValues(getTColumnValue(HBASE_DATABASE_COLUMN_QUERY_ROW_NAMES,hashCodeString,hashCodeString));
		rowNamesPut.setRow(HBASE_DATABASE_ROW_QUERY_ROW_NAMES);
		
		List<TPut> list=new ArrayList<TPut>();
		list.add(queryPut);
		list.add(rowNamesPut);
		client.putMultiple(tableNameBuffer, list);
	}
	@Override
	protected void writeData(ByteBuffer tableNameBuffer, Client client) {
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
		} catch(TIOError ioe) {
			logger.error("DatabaseData "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
