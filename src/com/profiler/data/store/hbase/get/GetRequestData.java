package com.profiler.data.store.hbase.get;

import static com.profiler.config.TomcatProfilerReceiverConstant.DATE_FORMAT_SHOW_YMD_HM;
import static com.profiler.config.TomcatProfilerReceiverConstant.DATE_FORMAT_YMD_HM;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_ELAPSED_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_DATA;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_URL;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_RESPONSE_TIME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_COLUMN_REQUEST_PARAMS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TCell;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.profiler.data.read.RequestDataPrinter;
import com.profiler.data.store.hbase.local.LocalDataManager;
import com.profiler.dto.RequestDataListThriftDTO;
import com.profiler.util.Converter;

public class GetRequestData extends AbstractGetData {
	long timestamp=-1;
	long period=600000;
	long minutes;
	public GetRequestData(long timestamp,long minutes) {
		super("Request");
		this.timestamp=timestamp;
		this.minutes=minutes;
		this.period=minutes*60*1000;
	}
	public Hashtable<String,StringBuilder> summaryStatisticsData=new Hashtable<String,StringBuilder>();
	Map<String,List<Long>> summaryDataMap=new HashMap<String,List<Long>>();
	public List<String> agentNameList=new ArrayList<String>();
	public StringBuilder scatterChartData=new StringBuilder();
	public long totalDataCount=-1;
	public List<CharSequence> slowRequestDataList=new ArrayList<CharSequence>();
	
	int pos=0;
	private final int indexElapsedTime=pos++;//0:elapsedTime
	private final int indexRequestParams=pos++;//1:requestParams
	private final int indexRequestTime=pos++;//2:requestTime
	private final int indexRequestURL=pos++;//3:requestURL
	private final int indexResponseTime=pos++;//4:responseTime
	@Override
	public CharSequence getData() {
		StringBuilder data=new StringBuilder();
		Hbase.Client client=getClient();
		if(client!=null) {
			try {
				totalDataCount=0;
				GetServerData getServer=new GetServerData();
				List<ByteBuffer> agentList=getServer.getServerList(client);
//				String rowName=DATE_FORMAT_YMD.format(new Date(dataTime));
				long fromTime=timestamp-period;
				List<ByteBuffer> rows=new ArrayList<ByteBuffer>();
				for(int loop=1;loop<=minutes;loop++) {
					String tempTimeString=DATE_FORMAT_YMD_HM.format(new Date(fromTime+loop*60000));
//					log("Time="+tempTimeString);
					rows.add(Converter.toByteBuffer(tempTimeString));
				}
				scatterChartData.append("[");
				for(ByteBuffer agent:agentList) {
					
					String agentHashCode=Converter.toString(agent);
					agentNameList.add(agentHashCode);
					String tableName=HBASE_REQUEST_TABLE+"_"+agentHashCode;
					ByteBuffer tableNameBuffer=ByteBuffer.wrap(tableName.getBytes());
					
					long startTime=System.nanoTime();
//					List<TRowResult> dataList=client.getRow(tableNameBuffer,rowNameBuffer);
					
//					List<TRowResult> dataList=client.getRows(tableNameBuffer, rows);
					List<ByteBuffer> columnList=new ArrayList<ByteBuffer>();
					columnList.add(Converter.toByteBuffer(HBASE_REQUEST_COLUMN_REQUEST_TIME));
					columnList.add(Converter.toByteBuffer(HBASE_REQUEST_COLUMN_RESPONSE_TIME));
					columnList.add(Converter.toByteBuffer(HBASE_REQUEST_COLUMN_ELAPSED_TIME));
					columnList.add(Converter.toByteBuffer(HBASE_REQUEST_COLUMN_REQUEST_URL));
					columnList.add(Converter.toByteBuffer(HBASE_REQUEST_COLUMN_REQUEST_PARAMS));
					List<TRowResult> dataList=client.getRowsWithColumns(tableNameBuffer, rows, columnList);
					
					long endTime=System.nanoTime();
					int dataListSize=dataList.size();
					double getTime=(endTime-startTime)/1000000.0;
					log("Request getDataTime="+getTime);
					
					int columnListSize=columnList.size();
					data.append("RowData count=").append(dataListSize).append("<BR>");
					if(dataList!=null && dataListSize>0) {
						for(TRowResult rowResult:dataList) {
							int columnSize=rowResult.getColumnsSize();
//							data.append("Reqeust Data Size:").append(columnSize).append(NEW_LINE);
							Map<ByteBuffer,TCell> columns=rowResult.getColumns();
							String keyList[]=getDataKeyList(columnSize,columns);
							
							int perColumnDataCount=columnSize/(columnListSize);
							totalDataCount+=perColumnDataCount;
							
							//0:elapsedTime
							//1:requestTime
							//2:requestURL
							//3:responseTime
							
//							data.append(keyList[0]).append(NEW_LINE);
//							data.append(keyList[perColumnDataCount]).append(NEW_LINE);
//							data.append(keyList[perColumnDataCount*2]).append(NEW_LINE);
//							data.append(keyList[perColumnDataCount*3]).append(NEW_LINE);
//							data.append(keyList[perColumnDataCount*4]).append(NEW_LINE);
							
							
							// Set scatter chart data
							int maxValuePos=appendScatterChartData(fromTime,indexResponseTime,indexElapsedTime,indexRequestURL,perColumnDataCount,columns,keyList,scatterChartData);
							if(maxValuePos!=-1) {
								setSlowRequestData(maxValuePos,perColumnDataCount,agentHashCode,rowResult,columns,keyList,tableNameBuffer,client);
							}
								
							setSummaryMap(2,0,perColumnDataCount,columns,keyList);
//							graphDataTable.put(tableName, scatterChartData);
						}
					}
				}
				getStatisticsData();
				
				
				data.append("Data Period:").append(DATE_FORMAT_SHOW_YMD_HM.format(new Date(fromTime)));
				data.append(" ~ ").append(DATE_FORMAT_SHOW_YMD_HM.format(new Date(timestamp))).append(NEW_LINE);
				data.append("Total data count:").append(totalDataCount).append(NEW_LINE);
				scatterChartData.append("]");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	protected int appendScatterChartData(long fromXValue,int fromX,int fromY,int tooltipPos,int perColumnDataCount,Map<ByteBuffer,TCell> columns,String keyList[],StringBuilder builder) {
		int maxYValuePos=-1;
		long maxValue=Long.MIN_VALUE;
		for(int loop=0;loop<perColumnDataCount;loop++) {
			long xValue=getLongValue(fromX*perColumnDataCount+loop,columns,keyList);
			long yPos=getLongValue(fromY*perColumnDataCount+loop,columns,keyList);
			
			long xPos=xValue-fromXValue;
			builder.append("[");
			builder.append(xPos).append(",");
			builder.append(yPos).append(",");
			builder.append("'blue'");
			if(tooltipPos!=-1 && yPos>1000) {
				String tooltipString=getStringValue(tooltipPos*perColumnDataCount+loop,columns,keyList);
				builder.append(",'").append(tooltipString).append(" ");
				builder.append(yPos/1000.0).append(" sec'");
			}
			builder.append("],");
			if(yPos>maxValue) {
				maxYValuePos=loop;
				maxValue=yPos;
			}
		}
		return maxYValuePos;
	}
	public long zeroToOne[]=new long[10];
	private void setSummaryMap(int urlPos,int elapsedTimePos,int perColumnDataCount,Map<ByteBuffer,TCell> columns,String keyList[]) {
		for(int loop=0;loop<perColumnDataCount;loop++) {
			String url=getStringValue(urlPos*perColumnDataCount+loop,columns,keyList);
			long elapsedTime=getLongValue(elapsedTimePos*perColumnDataCount+loop,columns,keyList);
			List<Long> elapsedTimeList;
			if(summaryDataMap.containsKey(url)) {
				elapsedTimeList=summaryDataMap.get(url);
			} else {
				elapsedTimeList=new ArrayList<Long>();
			}
			if(elapsedTime<1000) {
				int index=(int)elapsedTime/100;
				zeroToOne[9-index]++;
//				System.out.println(elapsedTime+" "+index);
			}
			elapsedTimeList.add(elapsedTime);
			summaryDataMap.put(url, elapsedTimeList);
		}
	}
	private void getStatisticsData() {
		Set<String> keySet=summaryDataMap.keySet();
		Percentile percentile=new Percentile();
		for(String key:keySet) {
			StringBuilder data=new StringBuilder();
			List<Long> elapsedTimeList=summaryDataMap.get(key);
			int callCount=elapsedTimeList.size();
			data.append("<TR><TD>").append(key).append("</TD>");
			data.append("<TD align=right>").append(callCount).append("</TD>");
			long min=Long.MAX_VALUE;
			long max=Long.MIN_VALUE;
			long sum=0;
			double []elapsedTimeArray=new double[callCount];
			for(int loop=0;loop<callCount;loop++) {//Long elapsedTime:elapsedTimeList) {
				long elapsedTime=elapsedTimeList.get(loop);
				sum+=elapsedTime;
				elapsedTimeArray[loop]=elapsedTime;
				if(elapsedTime<min) min=elapsedTime;
				if(elapsedTime>max) {
					max=elapsedTime;
				}
			}
			
			data.append("<TD align=right>").append(min).append("</TD>");
			data.append("<TD align=right>").append(max).append("</TD>");
			data.append("<TD align=right>").append(Converter.toDecimalDot(sum/1000.0)).append("</TD>");
			data.append("<TD align=right>").append(Converter.toDecimalDot(1.0*sum/callCount)).append("</TD>");
			data.append("<TD align=right>").append(Converter.toDecimalDot(percentile.evaluate(elapsedTimeArray, 90))).append("</TD>");
			data.append("</TR>");
			summaryStatisticsData.put(key, data);
		}
	}
	private static final String NEW_LINE="<BR>"; 
	private void setSlowRequestData(int maxValuePos,int perColumnDataCount,String agentHashCode,
			TRowResult rowResult,Map<ByteBuffer,TCell> columns,
			String[] keyList,ByteBuffer tableNameBuffer,Hbase.Client client)  {
		try {
			String urlKey=keyList[perColumnDataCount*indexRequestURL+maxValuePos];
			
			String []keys=seperateFamilyQualifier(urlKey);
			String key=keys[1];
			byte[] maxRow=rowResult.getRow();
			List<TCell> maxDataList=client.get(tableNameBuffer, ByteBuffer.wrap(maxRow), Converter.toByteBuffer(HBASE_REQUEST_COLUMN_REQUEST_DATA+key)) ;
			
			if(maxDataList!=null && maxDataList.size()>0) {
				TCell elapsedTimeCell=columns.get(Converter.toByteBuffer(keyList[perColumnDataCount*indexElapsedTime+maxValuePos]));
				TCell reqParamsCell=columns.get(Converter.toByteBuffer(keyList[perColumnDataCount*indexRequestParams+maxValuePos]));
				TCell fromTimeCell=columns.get(Converter.toByteBuffer(keyList[perColumnDataCount*indexRequestTime+maxValuePos]));
				TCell toTimeCell=columns.get(Converter.toByteBuffer(keyList[perColumnDataCount*indexResponseTime+maxValuePos]));
				
				TCell maxData=maxDataList.get(0);
				byte[] dtoBytes=maxData.getValue();
					
				CharSequence agentName=LocalDataManager.getAgentName(client, Integer.parseInt(agentHashCode));
				TCell urlCell=columns.get(Converter.toByteBuffer(urlKey));
				String url=new String(urlCell.getValue());
				byte[] fromTime=fromTimeCell.getValue();
				byte[] toTime=toTimeCell.getValue();
				byte[] elapsedTime=elapsedTimeCell.getValue();
				String reqParams=new String(reqParamsCell.getValue());
				
				StringBuilder requestDataString=new StringBuilder();
				requestDataString.append("Agent Name:");
				requestDataString.append(agentName);
				requestDataString.append(NEW_LINE);
				requestDataString.append("URL:");
				requestDataString.append(url);
				requestDataString.append(NEW_LINE);
				requestDataString.append("Request Params:");
				requestDataString.append(reqParams);
				requestDataString.append(NEW_LINE);
				requestDataString.append("Request Time:");
				requestDataString.append(Converter.toHmsMs(fromTime));
				requestDataString.append(NEW_LINE);
				requestDataString.append("Response Time:");
				requestDataString.append(Converter.toHmsMs(toTime));
				requestDataString.append(NEW_LINE);
				requestDataString.append("Elapsed Time:");
				requestDataString.append(Converter.toDecimalComma(elapsedTime));
				requestDataString.append(NEW_LINE);
					
				if(dtoBytes.length!=0) {
					requestDataString.append("<HR>");
					TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
					RequestDataListThriftDTO dto=new RequestDataListThriftDTO();
					deserializer.deserialize(dto,dtoBytes);
					RequestDataPrinter.printRequestData(dto, client);
				}
				slowRequestDataList.add(requestDataString);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
