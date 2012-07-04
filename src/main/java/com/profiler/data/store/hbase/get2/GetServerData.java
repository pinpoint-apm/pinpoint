package com.profiler.data.store.hbase.get2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_INSTANCE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVER_GROUP_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_SERVICE_NAME;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_ROW_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE_BYTE_BUFFER;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.thrift2.generated.TColumnValue;
import org.apache.hadoop.hbase.thrift2.generated.TGet;
import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TResult;

import com.profiler.util.Converter;
public class GetServerData extends AbstractGetData {
	public GetServerData() {
		super("Server");
	}
	public CharSequence getData() {
		StringBuilder data=new StringBuilder();
		THBaseService.Client client=getClient();
		if(client!=null) {
			try {
				
				List<TGet> rowNameList=getServerList(client);
				
				List<TResult> serverList=client.getMultiple(HBASE_SERVER_TABLE_BYTE_BUFFER,rowNameList);
//				System.out.println(serverList);
				data.append("<Table border=1><TR><TD>");
				data.append("IP").append("</TD><TD>");
				data.append("Ports").append("</TD><TD>");
				data.append("Status").append("</TD><TD>");
				data.append("EventTime").append("</TD><TD>");
				data.append("RunningTime").append("</TD><TD>");
				data.append("instanceName").append("</TD><TD>");
				data.append("serviceName").append("</TD><TD>");
				data.append("serviceGroupName").append("</TD>");
				data.append("<TD>Change value</TD></TR>");
				int index=0;
				for(TResult server:serverList) {
					data.append("<TR><TD>");
					String serverHashCode=new String(server.getRow());
					List<TColumnValue> columnList=server.getColumnValues();
					Map<String,String> columnMap=Converter.columnListToMap(columnList,null,true);
					long timestamp=columnList.get(0).getTimestamp();
					String ip=columnMap.get(HBASE_SERVER_COLUMN_IP);
					if(ip!=null) {
						String isRunning=columnMap.get(HBASE_SERVER_COLUMN_IS_RUNNING);
						data.append(ip).append("</TD><TD>");
						data.append(columnMap.get(HBASE_SERVER_COLUMN_PORTS)).append("</TD><TD>");
						if(isRunning.equals("true")) {
							data.append("Running").append("</TD><TD>");
						} else {
							data.append("Down").append("</TD><TD>");
						}
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
						
						
						String serviceName=columnMap.get(HBASE_SERVER_COLUMN_SERVICE_NAME);
						if(serviceName==null) {
							data.append("<TD><input id=\"instanceName").append(index).append("\"></TD>");
							data.append("<TD><input name=\"serviceName").append(index).append("\"></TD>");
							data.append("<TD><input name=\"serverGroupName").append(index).append("\"></TD>");
							data.append("<TD><input type=button value=Insert onclick=\"javascript:updateServer('").append(serverHashCode).append("','").append(index).append("')\"></TD>");
						} else {
							String instanceName=columnMap.get(HBASE_SERVER_COLUMN_INSTANCE_NAME);
							String serverGroupName=columnMap.get(HBASE_SERVER_COLUMN_SERVER_GROUP_NAME);
							data.append("<TD><input name=\"instanceName").append(serverHashCode).append("\" value=\"").append(instanceName).append("\"></TD>");
							data.append("<TD><input name=\"serviceName").append(serverHashCode).append("\" value=\"").append(serviceName).append("\"></TD>");
							data.append("<TD><input name=\"serverGroupName").append(serverHashCode).append("\" value=\"").append(serverGroupName).append("\"></TD>");
							data.append("<TD><input type=button value=Update onclick=\"javascript:updateServer('").append(serverHashCode).append("','").append(index).append("')\"></TD>");
							
						}
					}
					data.append("</TR>");
					index++;
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
	public List<TGet> getServerList(THBaseService.Client client) throws Exception{
		//
		List<TGet> gets=new ArrayList<TGet>();
		TGet rowNamesGet=new TGet();
//		List<TColumn> columns=new ArrayList<TColumn>();
//		TColumn colIP=new TColumn(Converter.toByteBuffer(HBASE_SERVER_COLUMN_IP));
//		columns.add(colIP);
//		rowNamesGet.setColumns(columns);
		rowNamesGet.setRow(HBASE_SERVER_ROW_ROW_NAMES);
		gets.add(rowNamesGet);
		TResult rowResult=client.get(HBASE_SERVER_TABLE_BYTE_BUFFER, rowNamesGet);
//		System.out.println(rowResult);
		List<TGet> agentHashCodeList=new ArrayList<TGet>();
		List<TColumnValue> columnValues=rowResult.getColumnValues();
		for(TColumnValue columnValue:columnValues) {
			byte[] tempValue=columnValue.getValue();
//				ByteBuffer tempData=ByteBuffer.wrap(tempValue);
			TGet tempGet=new TGet();
			tempGet.setRow(tempValue);
			agentHashCodeList.add(tempGet);
			
			//For debugging. After debug, it can remove
//			String value=new String(tempValue);
//			log("Server rowName="+value);
		}
		return agentHashCodeList;
	}
	
}
