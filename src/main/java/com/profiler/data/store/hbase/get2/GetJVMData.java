package com.profiler.data.store.hbase.get2;

import static com.profiler.config.TomcatProfilerReceiverConstant.DATE_FORMAT_YMD;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC1_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_GC2_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_COLUMN_PROCESS_CPU_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_DATA_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_DATA_GC_COUNT;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_DATA_HEAP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_REQUEST_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_COLUMN_RESPONSE_TPS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_TABLE;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.thrift2.generated.TGet;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TResult;
import org.apache.hadoop.hbase.thrift2.generated.TTimeRange;

import com.profiler.data.store.hbase.local2.LocalDataManager;
import com.profiler.util.Converter;

public class GetJVMData extends AbstractGetData {
	long dataTime=0;
	long dataPeriod=0;
	public GetJVMData(long dataTime,long dataPeriod) {
		super("JVM");
		this.dataTime=dataTime;
		this.dataPeriod=dataPeriod; 
	}
	public Hashtable<String,StringBuilder> graphDataTable=new Hashtable<String,StringBuilder>();
	public List<String> agentHashCodeList=new ArrayList<String>();
	public List<String> agentStringNameList=new ArrayList<String>();
	@Override
	public CharSequence getData() {
		StringBuilder data=new StringBuilder();
		THBaseService.Client client=getClient();
		if(client!=null) {
			try {
				GetServerData getServer=new GetServerData();
				List<TGet> agentList=getServer.getServerList(client);
				String rowName=DATE_FORMAT_YMD.format(new Date(dataTime));
				ByteBuffer rowNameBuffer=Converter.toByteBuffer(rowName);
				data.append("Data Date:").append(rowName).append(SPACES);
				
				for(TGet agentGet:agentList) {
					byte[] agent=agentGet.getRow();
					String agentHashCode=new String(agent);
					agentHashCodeList.add(agentHashCode);
					agentStringNameList.add(LocalDataManager.getAgentName(client, agentHashCode));
					data.append("Agent HashCode:").append(agentHashCode).append(NEW_LINE);

					String jvmTableName=HBASE_JVM_TABLE+"_"+agentHashCode;
					ByteBuffer jvmTableNameBuffer=ByteBuffer.wrap(jvmTableName.getBytes());
					TGet get=new TGet();
					get.setRow(rowNameBuffer);
					if(dataPeriod!=-1) {
						TTimeRange timeRange=new TTimeRange();
						timeRange.setMinStamp(dataTime-dataPeriod);
						timeRange.setMaxStamp(dataTime);
						get.setTimeRange(timeRange);
					}
					long startTime=System.nanoTime();
					TResult jvmDataList=client.get(jvmTableNameBuffer,get);
					long endTime=System.nanoTime();
					double getTime=(endTime-startTime)/1000000.0;
					log("JVM getDataTime="+getTime);
					
					String tpsTableName=HBASE_TPS_TABLE+"_"+agentHashCode;
					ByteBuffer tpsTableNameBuffer=ByteBuffer.wrap(tpsTableName.getBytes());
					
					startTime=System.nanoTime();
					TResult tpsDataList=client.get(tpsTableNameBuffer,get);
					endTime=System.nanoTime();
					getTime=(endTime-startTime)/1000000.0;
					log("TPS getDataTime="+getTime);
					
					//TODO Period data fetch part must implemented !!!!!!!!!!
					
					if(jvmDataList!=null ) {
//						TRowResult rowResult=jvmDataList.get(0);
						Map<String,String> columns=Converter.tResultToMap(jvmDataList, null, false);
						int columnSize=columns.size();
						data.append("JVM Data Size:").append(columnSize).append(NEW_LINE);
						
						String keyList[]=getDataKeyList(columnSize,columns);
						
						int perColumnDataCount=columnSize/HBASE_JVM_DATA_COUNT;
						//0:activeThreadCount
						//1:gc1Count
						//2:gc1Time
						//3:gc2Count
						//4:gc2Time
						//5:heapCommitted
						//6:heapUsed
						//7:nonHeapCommitted
						//8:nonHeapUsed
						//9:processCPUTime
//						data.append(keyList[0]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*2]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*3]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*4]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*5]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*6]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*7]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*8]).append(NEW_LINE);
//						data.append(keyList[perColumnDataCount*9]).append(NEW_LINE);
						
						StringBuilder activeThreadCountData=new StringBuilder();
						StringBuilder heapUsedData=new StringBuilder();
						StringBuilder processCPUData=new StringBuilder();
						StringBuilder gc1TimeData=new StringBuilder();
						StringBuilder gc2TimeData=new StringBuilder();
						StringBuilder gcCountData=new StringBuilder();
						
						appendLineGraphData(0,perColumnDataCount,columns,keyList,activeThreadCountData);
						appendScatterChartValueGapData(2,perColumnDataCount,columns,keyList,gc1TimeData);
						appendGCCountData(perColumnDataCount,columns,keyList,gcCountData);
						appendScatterChartValueGapData(4,perColumnDataCount,columns,keyList,gc2TimeData);
						appendLineGraphData(6,perColumnDataCount,columns,keyList,heapUsedData);
						appendLineGraphData(9,perColumnDataCount,columns,keyList,processCPUData);
						
						graphDataTable.put(HBASE_JVM_COLUMN_ACTIVE_THREAD_COUNT+agentHashCode, activeThreadCountData);
						graphDataTable.put(HBASE_JVM_DATA_HEAP+agentHashCode, heapUsedData);
						graphDataTable.put(HBASE_JVM_COLUMN_PROCESS_CPU_TIME+agentHashCode, processCPUData);
						graphDataTable.put(HBASE_JVM_COLUMN_GC1_TIME+agentHashCode, gc1TimeData);
						graphDataTable.put(HBASE_JVM_COLUMN_GC2_TIME+agentHashCode, gc2TimeData);
						graphDataTable.put(HBASE_JVM_DATA_GC_COUNT+agentHashCode, gcCountData);
						
					}
					if(tpsDataList!=null ) {
						
						int columnSize=tpsDataList.getColumnValuesSize();
						data.append("TPS Data Size:").append(columnSize).append(NEW_LINE);
						
						Map<String,String> columns=Converter.tResultToMap(tpsDataList, null, false);
						String keyList[]=getDataKeyList(columnSize,columns);
						
						int perColumnDataCount=columnSize/2;
						
						StringBuilder reqData=new StringBuilder();
						StringBuilder resData=new StringBuilder();
						appendLineGraphData(0,perColumnDataCount,columns,keyList,reqData);
						appendLineGraphData(1,perColumnDataCount,columns,keyList,resData);
						graphDataTable.put(HBASE_TPS_COLUMN_REQUEST_TPS+agentHashCode, reqData);
						graphDataTable.put(HBASE_TPS_COLUMN_RESPONSE_TPS+agentHashCode, resData);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	protected void appendScatterChartValueGapData(int from,int perColumnDataCount,Map<String,String> columns,String keyList[],StringBuilder builder) {
		long previousValue=getLongValue(from*perColumnDataCount,columns,keyList);
//		System.out.println(previousValue);
		builder.append("[");
		for(int loop=1;loop<perColumnDataCount;loop++) {
			long currentValue=getLongValue(from*perColumnDataCount+loop,columns,keyList);
			long gap=currentValue-previousValue;
			if(gap!=0) {
				builder.append("[");
				builder.append(loop).append(",");
				builder.append(gap).append(",");
				builder.append("'blue'");
				builder.append("],");
//				System.out.println(gap); 
				previousValue=currentValue;
			}
		}
		builder.append("]");
	}
	private void appendGCCountData(int perColumnDataCount,Map<String,String> columns,String keyList[],StringBuilder gcCountData) {
		String gc1Count=columns.get(keyList[perColumnDataCount*2-1]);
		String gc2Count=columns.get(keyList[perColumnDataCount*4-1]);
		gcCountData.append(gc1Count)
		.append(",")
		.append(gc2Count);
	}
	 
	
}
