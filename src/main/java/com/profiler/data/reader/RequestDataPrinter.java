package com.profiler.data.reader;

import java.util.Date;
import java.util.List;

import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.dto.RequestDataThriftDTO;

public class RequestDataPrinter {
	public static StringBuilder printRequestData(RequestDataListThriftDTO dto,CharSequence agentName,String url,String newLine) {
		StringBuilder message=new StringBuilder();
//		message.append("########################################\n");
		
//		logger.debug(manager.getAgentName(hostHashCode));
		message.append(agentName).append(newLine);
		
		
//		logger.debug(manager.getRequestURL(requestHashCode));
		message.append(url).append(newLine);
		
		List<RequestDataThriftDTO> list=dto.getRequestDataList();
		if(list!=null) {
			
			for(RequestDataThriftDTO tempDataDto:list) {
				int dataType=tempDataDto.getDataType();
				String dataTypeString=printDataType(dataType);
				
				long dataTime=tempDataDto.getDataTime();
				String dataTimeString=TomcatProfilerReceiverConstant.DATE_FORMAT_HMS_MS.format(new Date(dataTime));
				
				int dataHashCode=tempDataDto.getDataHashCode();
				String dataString=tempDataDto.getDataString();
				
				int extraInt1=tempDataDto.getExtraInt1();
				int extraInt2=tempDataDto.getExtraInt2();
				
				message.append(dataTimeString).append(" ")
				.append(dataTypeString).append(" ");
				if(dataHashCode!=0) {
					message.append(dataHashCode).append(" ");
				}
				if(dataString!=null) {
					message.append(dataString).append(" ");
				}
				if(extraInt1!=0) {
					message.append(extraInt1).append(" ");
				}
				if(extraInt2!=0) {
					message.append(extraInt2).append(" ");
				}
				message.append(newLine);
				if(dataType==TomcatProfilerReceiverConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION) {
					message.append("------------------------------").append(newLine);
				}
			}
		}
		return message;
	}
	public static String printDataType(int dataType) {
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
			return "FETCH Data";
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
