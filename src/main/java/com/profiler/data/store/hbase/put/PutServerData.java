package com.profiler.data.store.hbase.put;

import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_ROW_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_ROW_NAMES;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IP;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_IS_RUNNING;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_COLUMN_PORTS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.IOError;
import org.apache.hadoop.hbase.thrift.generated.Mutation;

import com.profiler.dto.AgentInfoDTO;
public class PutServerData extends AbstractPutData{
	AgentInfoDTO dto=null;
	public PutServerData(String tableName,AgentInfoDTO dto) {
		super(tableName);
		this.dto=dto;
	}
	public void writeData(ByteBuffer tableNameBuffer,Hbase.Client client) {
		try {
			int agentHashCode=dto.getHostHashCode();
			String agentHashCodeString=agentHashCode+"";
			ByteBuffer row=ByteBuffer.wrap(agentHashCodeString.getBytes());
			
			long timestamp=dto.getTimestamp();
			List<Mutation> mutations = new ArrayList<Mutation>(); 
			mutations.add(getMutation(HBASE_SERVER_COLUMN_IP+agentHashCode,dto.getHostIP().getBytes()));
			mutations.add(getMutation(HBASE_SERVER_COLUMN_PORTS+agentHashCode,dto.getPortNumbers().getBytes()));
			mutations.add(getMutation(HBASE_SERVER_COLUMN_IS_RUNNING+agentHashCode,(dto.isAlive()+"").getBytes()));
			client.mutateRowTs(tableNameBuffer, row, mutations, timestamp);

			List<Mutation> rowNamesMutation=new ArrayList<Mutation>();
			rowNamesMutation.add(getMutation(HBASE_SERVER_COLUMN_ROW_NAMES+agentHashCodeString,agentHashCodeString.getBytes()));
			client.mutateRowTs(tableNameBuffer, HBASE_SERVER_ROW_ROW_NAMES, rowNamesMutation, timestamp);
		} catch(IOError ioe) {
			logger.error("ServerData "+ioe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
