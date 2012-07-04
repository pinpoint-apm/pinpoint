package com.profiler.data.read2;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService;

import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.data.store.hbase.local2.LocalDataManager;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestDataThriftDTO;
import com.profiler.util.Converter;

public class RequestDataPrinter {
	private static final String NEW_LINE="<BR>"; 
	public static StringBuilder printRequestData(RequestDataListThriftDTO dto,THBaseService.Client client) {
		StringBuilder message=new StringBuilder();
//		message.append("########################################\n");
		
		List<RequestDataThriftDTO> list=dto.getRequestDataList();
		if(list!=null) {
			StringBuilder preparedQuery=null;
			for(RequestDataThriftDTO tempDataDto:list) {
				int dataType=tempDataDto.getDataType();
				String dataTypeString=printDataType(dataType);
				
				long dataTime=tempDataDto.getDataTime();
				String dataTimeString=Converter.toHmsMs(dataTime);
				
				int dataHashCode=tempDataDto.getDataHashCode();
//				System.out.println(dataTypeString+" "+dataHashCode);
 				if(dataType!=TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM) {
					message.append(dataTimeString).append(" ")
					.append(dataTypeString).append(" ");
 				}
//				if(dataHashCode!=0) {
//					message.append(dataHashCode).append(" ");
//				}
				if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_GET_CONNECTION) {
					message.append("<B>[").append(LocalDataManager.getDB(client, dataHashCode)).append("]</B> ");
				} else if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_QUERY) {
					StringBuilder tempMessage=new StringBuilder();
					tempMessage.append("<B>[").append(LocalDataManager.getQuery(client, dataHashCode)).append("]</B> ");
					preparedQuery=tempMessage;
				}
				String dataString=tempDataDto.getDataString();
				if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM) {
					if(dataString==null) {
						if(preparedQuery!=null) {
							message.append(preparedQuery);
							preparedQuery=null;
						}
					} else {
						if(preparedQuery!=null) {
							message.append(changePreparedQuery(preparedQuery,dataString));
							preparedQuery=null;
						} else {
							message.append(dataString).append(" ");
						}
					}
				}
				int extraInt1=tempDataDto.getExtraInt1();
				int extraInt2=tempDataDto.getExtraInt2();
				if(extraInt1!=0) {
					if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_FETCH) {
						message.append("Current Fetch=");
					}
					message.append("[").append(extraInt1).append("] ");
				}
				if(extraInt2!=0) {
					if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_FETCH) {
						message.append("Total Fetch=");
					}
					message.append("[").append(extraInt2).append("] ");
				}
				message.append(NEW_LINE);
				if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION) {
					message.append("------------------------------").append(NEW_LINE);
				}
			}
		}
		return message;
	}
	private static StringBuilder changePreparedQuery(StringBuilder preparedQuery,String dataString) {
		StringBuilder result=new StringBuilder();
		try {
			StringTokenizer query=new StringTokenizer(preparedQuery.toString(),"?");
			StringTokenizer param=new StringTokenizer(dataString,",");
			while(query.hasMoreTokens()) {
				result.append(query.nextToken());
				if(param.hasMoreTokens()) {
					result.append("<FONT color=red>").append(param.nextToken()).append("</FONT>");
				} else {
	//				result.append("?");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private static String printDataType(int dataType) {
		switch(dataType) {
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_GET_CONNECTION:
			return "DB CONNECTION";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT:
			return "CREATE STATEMENT";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_GET_PREPARED_STATEMENT:
			return "CREATE PREPARED STATEMENT";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_QUERY:
			return "QUERY";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY:
			return "EXECUTE QUERY";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_EXECUTE_UPDATE:
			return "EXECUTE UPDATE";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_FETCH:
			return "FETCH DB";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_RESULTSET_CLOSE:
			return "CLOSE RESULTSET";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_PREPARED_STATEMENT_PARAM:
			return "PREPARED STATEMENT PARAM";
		case TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION:
			return "CLOSE CONNECTION";
			
		}
		return "Not Defined";
	}
}
