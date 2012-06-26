package com.profiler.data.store.hbase.put2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_CLIENT_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService.Client;
import org.apache.hadoop.hbase.thrift2.generated.TIOError;
import org.apache.hadoop.hbase.thrift2.generated.TPut;

import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.dto.RequestThriftDTO;
public class PutRequestTransactionData extends AbstractPutData{
	List<RequestThriftDTO> requestDtoList=null;
	List<RequestThriftDTO> responseDtoList=null;
	List<byte[]> responseDataByteArrayList=null;
	public PutRequestTransactionData(String tableName,
			List<RequestThriftDTO> requestDtoList,
			List<RequestThriftDTO> responseDtoList,
			List<byte[]> responseDataByteArrayList) {
		super(tableName);
		this.requestDtoList=requestDtoList;
		this.responseDtoList=responseDtoList;
		this.responseDataByteArrayList=responseDataByteArrayList;
	}
	
	@Override
	protected void writeData(ByteBuffer tableNameBuffer, Client client) {
		try {
			RequestThriftDTO tempDto=responseDtoList.get(0);
			long tempTime=tempDto.getDataTime();
			long timestamp=tempTime-tempTime%1000;
			String time=TomcatProfilerReceiverConstant.DATE_FORMAT_YMD_HM.format(new Date(tempTime));
//			System.out.println(timestamp+" "+time);
			
			ByteBuffer row=ByteBuffer.wrap(time.getBytes());

			int listSize=requestDtoList.size();
			List<TPut> list=new ArrayList<TPut>();
			for(int loop=0;loop<listSize;loop++) {
				RequestThriftDTO requestDto=requestDtoList.get(loop);
				RequestThriftDTO responseDto=responseDtoList.get(loop);
				long requestHashCode=requestDto.getRequestHashCode();
				long requestTime=requestDto.getDataTime();
				long responseTime=responseDto.getDataTime();
				long elapsedTime=responseTime-requestTime;
				String url=requestDto.getRequestURL();
				String clientIP=requestDto.getClientIP();
				String requestParams=requestDto.getExtraData1();
				
				TPut requestPut=new TPut();
				String requestHashCodeString=requestHashCode+"";
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_REQUEST_TIME,requestHashCodeString,requestTime+""));
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_RESPONSE_TIME,requestHashCodeString,responseTime+""));
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_ELAPSED_TIME,requestHashCodeString,elapsedTime+""));
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_REQUEST_URL,requestHashCodeString,url));
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_CLIENT_IP,requestHashCodeString,clientIP));
				
				String saveParams="";
				if(requestParams!=null) {
					saveParams=requestParams;
				}
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_REQUEST_PARAMS,requestHashCodeString,saveParams));
				
				byte[] tempByteData=null;
				if((tempByteData=responseDataByteArrayList.get(loop))==null) {
					tempByteData=new String().getBytes();
				}
				requestPut.addToColumnValues(getTColumnValue(HBASE_REQUEST_COLUMN_REQUEST_DATA,requestHashCodeString,tempByteData));
				
				requestPut.setRow(row);
				requestPut.setTimestamp(timestamp);

				list.add(requestPut);
				
			}
//			client.mutateRowsTs(tableNameBuffer, rowBatches,timestamp);
			client.putMultiple(tableNameBuffer, list);
		} catch(TIOError ioe) {
			logger.error("PutRequestTransactionData "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
}
