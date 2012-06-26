package com.profiler.data.store.hbase.put2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService.Client;
import org.apache.hadoop.hbase.thrift2.generated.TIOError;
import org.apache.hadoop.hbase.thrift2.generated.TPut;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.transport.TTransportException;

import com.profiler.config.TomcatProfilerReceiverConstant;
public class PutTPSData extends AbstractPutData{
	List<Integer[]> saveDataList;
	long lastDataTime;
	public PutTPSData(String tableName,List<Integer[]> saveDataList,long lastDataTime) {
		super(tableName);
		this.saveDataList=saveDataList;
		this.lastDataTime=lastDataTime;
	}
	
	@Override
	protected void writeData(ByteBuffer tableNameBuffer, Client client) {
		try {
			int dataSize=saveDataList.size();
			long standardTime=lastDataTime-lastDataTime%1000;
			long firstTime=standardTime-(dataSize-1)*1000;
			List<TPut> list=new ArrayList<TPut>();
			for(int loop=0;loop<dataSize;loop++) {
				long timestamp=firstTime+1000*loop;
				String timestampString=timestamp+"";
				String rowName=TomcatProfilerReceiverConstant.DATE_FORMAT_YMD.format(new Date(timestamp));
				ByteBuffer row=ByteBuffer.wrap(Bytes.toBytes(rowName));
				Integer[] tempData=saveDataList.get(loop);
				TPut tpsPut=new TPut();
				
				tpsPut.addToColumnValues(getTColumnValue(HBASE_TPS_COLUMN_REQUEST_TPS,timestampString,tempData[0]+""));
				tpsPut.addToColumnValues(getTColumnValue(HBASE_TPS_COLUMN_RESPONSE_TPS,timestampString,tempData[1]+""));
				tpsPut.setRow(row);
				tpsPut.setTimestamp(timestamp);
				list.add(tpsPut);
			}
			client.putMultiple(tableNameBuffer, list);
			
		} catch (TTransportException tte) {
			logger.error(tte);
			System.err.println(tte.getMessage());
		} catch(TIOError ioe) {
			logger.error("ServerData "+ioe.getMessage());
			System.err.println(ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
