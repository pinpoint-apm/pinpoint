package com.profiler.data.store.hbase.put;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.BatchMutation;
import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.IOError;
import org.apache.hadoop.hbase.thrift.generated.Mutation;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.config.TomcatProfilerReceiverConstant;
public class PutTPSData extends AbstractPutData{
	List<Integer[]> saveDataList;
	long lastDataTime;
	public PutTPSData(String tableName,List<Integer[]> saveDataList,long lastDataTime) {
		super(tableName);
		this.saveDataList=saveDataList;
		this.lastDataTime=lastDataTime;
	}
	public void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) {
		try {
			int dataSize=saveDataList.size();
			long standardTime=lastDataTime-lastDataTime%1000;
			long firstTime=standardTime-(dataSize-1)*1000;
//			logger.debug("firstTime="+firstTime+" standardTime="+standardTime);
			List<BatchMutation> rowBatches=new ArrayList<BatchMutation>(dataSize);
			for(int loop=0;loop<dataSize;loop++) {
				long timestamp=firstTime+1000*loop;
				String rowName=TomcatProfilerReceiverConstant.DATE_FORMAT_YMD.format(new Date(timestamp));
				ByteBuffer row=ByteBuffer.wrap(Bytes.toBytes(rowName));
				//OK from here
				Integer[] tempData=saveDataList.get(loop);
				List<Mutation> mutations = new ArrayList<Mutation>(); 
				mutations.add(getMutation(HBASE_TPS_COLUMN_REQUEST_TPS+timestamp,(tempData[0]+"").getBytes()));
				mutations.add(getMutation(HBASE_TPS_COLUMN_RESPONSE_TPS+timestamp,(tempData[1]+"").getBytes()));
				
				BatchMutation tempBatch=new BatchMutation(row,mutations);
				rowBatches.add(tempBatch);
				
			}
			client.mutateRowsTs(tableNameBuffer, rowBatches,standardTime);
		} catch(IOError ioe) {
			logger.error("ServerData "+ioe.getMessage());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
