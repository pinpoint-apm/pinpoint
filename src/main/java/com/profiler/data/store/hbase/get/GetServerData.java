package com.profiler.data.store.hbase.get;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVICE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVER_GROUP_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_ROW_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE_BYTE_BUFFER;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TCell;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;

import com.profiler.util.Converter;
public class GetServerData extends AbstractGetData {
	public GetServerData() {
		super("Server");
	}
	public CharSequence getData() {
		StringBuilder data=new StringBuilder();
		Hbase.Client client=getClient();
		if(client!=null) {
			try {
				
				List<ByteBuffer> rowNameList=getServerList(client);
				List<TRowResult> serverList=client.getRows(HBASE_SERVER_TABLE_BYTE_BUFFER, rowNameList);
				
				data.append("<Table border=1><TR><TD>");
				data.append("IP").append("</TD><TD>");
				data.append("Ports").append("</TD><TD>");
				data.append("Status").append("</TD><TD>");
				data.append("EventTime").append("</TD><TD>");
				data.append("RunningTime").append("</TD><TD>");
				data.append("instanceName").append("</TD><TD>");
				data.append("serviceName").append("</TD><TD>");
				data.append("serviceGroupName").append("</TD></TR>");
				for(TRowResult server:serverList) {
					Map<ByteBuffer,TCell> columnMap=server.getColumns();
//					Set<ByteBuffer> keySet=columnMap.keySet();
					data.append("<TR><TD>");
					String serverHashCode=new String(server.getRow());
					String ipKey=HBASE_SERVER_COLUMN_IP+serverHashCode;
					String portsKey=HBASE_SERVER_COLUMN_PORTS+serverHashCode;
					String isRunningKey=HBASE_SERVER_COLUMN_IS_RUNNING+serverHashCode;
					String serviceNameKey=HBASE_SERVER_COLUMN_SERVICE_NAME+serverHashCode;
//					log(ipKey);
					TCell ipValue=columnMap.get(Converter.toByteBuffer(ipKey));
					TCell portsValue=columnMap.get(Converter.toByteBuffer(portsKey));
					TCell isRunningValue=columnMap.get(Converter.toByteBuffer(isRunningKey));
					if(ipValue!=null) {
						String isRunning=new String(isRunningValue.getValue());
						data.append(new String(ipValue.getValue())).append("</TD><TD>");
						data.append(new String(portsValue.getValue())).append("</TD><TD>");
						if(isRunning.equals("true")) {
							data.append("Running").append("</TD><TD>");
						} else {
							data.append("Down").append("</TD><TD>");
						}
						long timestamp=isRunningValue.getTimestamp();
						data.append(new Date(timestamp)).append("</TD>");
						if(isRunning.equals("true")) {
							data.append("<TD>");
							long current=System.currentTimeMillis();
							long gap=current-timestamp;
							long sec=gap/1000;
							if(sec<60) {// under 1 min
								data.append(sec).append(" sec.");
							} else if(sec<3600) {// under 1 hour
								data.append(sec/60).append(" min.");
							} else if(sec<86400){
								data.append(sec/3600.0).append(" hour.");
							} else {
								data.append(sec/86400.0).append(" days.");
							}
							data.append("</TD>");
						} else {
							data.append("<TD align=center>-</TD>");
						}
						
						
						TCell serviceNameValue=columnMap.get(Converter.toByteBuffer(serviceNameKey));
						if(serviceNameValue==null) {
							data.append("<TD colspan=3 align=center>&nbsp;-&nbsp;</TD>");
						} else {
							String instanceNameKey=HBASE_SERVER_COLUMN_INSTANCE_NAME+serverHashCode;
							String serverGroupNameKey=HBASE_SERVER_COLUMN_SERVER_GROUP_NAME+serverHashCode;
							TCell instanceNameValue=columnMap.get(Converter.toByteBuffer(instanceNameKey));
							TCell serverGroupNameValue=columnMap.get(Converter.toByteBuffer(serverGroupNameKey));
							data.append("<TD>");
							data.append(new String(instanceNameValue.getValue())).append("</TD><TD>");
							data.append(new String(serviceNameValue.getValue())).append("</TD><TD>");
							data.append(new String(serverGroupNameValue.getValue())).append("</TD>");
						}
					}
					data.append("</TR>");
				}
				data.append("</Table>");
				
			} catch(Exception e) {
				e.printStackTrace();
			} 
		} else {
			data.append("<B><Font color=blue>Hbase.client is null</Font></B>");
		}
		return data;
	}
	public List<ByteBuffer> getServerList(Hbase.Client client) throws Exception{
		
		List<TRowResult> rowList=client.getRow(HBASE_SERVER_TABLE_BYTE_BUFFER, ByteBuffer.wrap(HBASE_SERVER_ROW_ROW_NAMES));
		List<ByteBuffer> agentHashCodeList=new ArrayList<ByteBuffer>();
		for(TRowResult result:rowList) {
			Map<ByteBuffer,TCell> agentMap=result.getColumns();
			Set<ByteBuffer> keySet=agentMap.keySet();
			for(ByteBuffer key:keySet) {
//				String keyString=new String(key.array());
				TCell tempValue=agentMap.get(key);
				ByteBuffer tempData=tempValue.bufferForValue();
				
				agentHashCodeList.add(tempData);
				//For debugging. After debug, it can remove
//				String value=new String(tempData.array());
//				log("Server rowName="+value);
			}
		}
//		client.getVer(tableName, row, column, numVersions);
		
		return agentHashCodeList;
	}
	
}
