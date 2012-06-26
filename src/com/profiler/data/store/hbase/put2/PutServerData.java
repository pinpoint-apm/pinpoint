package com.profiler.data.store.hbase.put2;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_ROW_ROW_NAMES;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift2.generated.THBaseService;
import org.apache.hadoop.hbase.thrift2.generated.TIOError;
import org.apache.hadoop.hbase.thrift2.generated.TPut;

import com.profiler.dto.AgentInfoDTO;
public class PutServerData extends AbstractPutData{
	AgentInfoDTO dto=null;
	public PutServerData(String tableName,AgentInfoDTO dto) {
		super(tableName);
		this.dto=dto;
	}
	public void writeData(ByteBuffer tableNameBuffer,THBaseService.Client client) {
		try {
			int agentHashCode=dto.getHostHashCode();
			String agentHashCodeString=agentHashCode+"";
			
			TPut serverPut=new TPut();
			
			serverPut.addToColumnValues(getTColumnValue(HBASE_SERVER_COLUMN_IP,agentHashCodeString,dto.getHostIP()));
			serverPut.addToColumnValues(getTColumnValue(HBASE_SERVER_COLUMN_PORTS,agentHashCodeString,dto.getPortNumbers()));
			serverPut.addToColumnValues(getTColumnValue(HBASE_SERVER_COLUMN_IS_RUNNING,agentHashCodeString,dto.isAlive()+""));
			serverPut.setRow(agentHashCodeString.getBytes());
			
			TPut rowNamesPut=new TPut();
			rowNamesPut.addToColumnValues(getTColumnValue(HBASE_SERVER_COLUMN_ROW_NAMES,agentHashCodeString,agentHashCodeString));
			rowNamesPut.setRow(HBASE_SERVER_ROW_ROW_NAMES);
			
			List<TPut> puts=new ArrayList<TPut>();
			puts.add(serverPut);
			puts.add(rowNamesPut);
			client.putMultiple(tableNameBuffer, puts);
			
		} catch(TIOError ioe) {
			logger.error("ServerData "+ioe.getMessage());
			System.out.println(ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
