package com.profiler.data.store.hbase.put;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.BatchMutation;
import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.IOError;
import org.apache.hadoop.hbase.thrift.generated.Mutation;

import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.dto.RequestThriftDTO;
import static com.profiler.config.TomcatProfilerReceiverConstant.*;
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
	public void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) {
		try {
			RequestThriftDTO tempDto=responseDtoList.get(0);
			long tempTime=tempDto.getDataTime();
			long timestamp=tempTime-tempTime%1000;
			String time=TomcatProfilerReceiverConstant.DATE_FORMAT_YMD_HM.format(new Date(tempTime));
//			System.out.println(timestamp+" "+time);
			
			ByteBuffer row=ByteBuffer.wrap(time.getBytes());

			int listSize=requestDtoList.size();
			List<BatchMutation> rowBatches=new ArrayList<BatchMutation>(listSize);
			for(int loop=0;loop<listSize;loop++) {
				List<Mutation> mutations = new ArrayList<Mutation>();
				RequestThriftDTO requestDto=requestDtoList.get(loop);
				RequestThriftDTO responseDto=responseDtoList.get(loop);
				long requestHashCode=requestDto.getRequestHashCode();
				long requestTime=requestDto.getDataTime();
				long responseTime=responseDto.getDataTime();
				long elapsedTime=responseTime-requestTime;
				String url=requestDto.getRequestURL();
				String clientIP=requestDto.getClientIP();
				String requestParams=requestDto.getExtraData1();
				mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_TIME+requestHashCode,(requestTime+"").getBytes()));
				mutations.add(getMutation(HBASE_REQUEST_COLUMN_RESPONSE_TIME+requestHashCode,(responseTime+"").getBytes()));
				mutations.add(getMutation(HBASE_REQUEST_COLUMN_ELAPSED_TIME+requestHashCode,(elapsedTime+"").getBytes()));
				mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_URL+requestHashCode,(url).getBytes()));
				mutations.add(getMutation(HBASE_REQUEST_COLUMN_CLIENT_IP+requestHashCode,(clientIP).getBytes()));
				if(requestParams!=null) { 
					mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_PARAMS+requestHashCode,(requestParams).getBytes()));
				} else {
					mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_PARAMS+requestHashCode,"".getBytes()));
				}
				byte[] tempByteData=null;
				if((tempByteData=responseDataByteArrayList.get(loop))!=null) {
					mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_DATA+requestHashCode,tempByteData));
//					System.out.println("Saved");
				} else {
					tempByteData=new String().getBytes();
					mutations.add(getMutation(HBASE_REQUEST_COLUMN_REQUEST_DATA+requestHashCode,tempByteData));
//					System.out.println("NULL");
				}
				BatchMutation tempBatch=new BatchMutation(row,mutations);
				rowBatches.add(tempBatch);
			}
			client.mutateRowsTs(tableNameBuffer, rowBatches,timestamp);
		} catch(IOError ioe) {
			logger.error("PutRequestTransactionData "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
