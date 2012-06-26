package com.profiler.data.store.hbase.create;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_JVM_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_DATABASE_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_REQUEST_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_SERVER_TABLE;
import static com.profiler.config.TomcatProfilerReceiverConstant.HBASE_TPS_TABLE;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.data.store.hbase.put.DataPutThreadManager;
import com.profiler.data.store.hbase.put.PutServerData;
import com.profiler.dto.AgentInfoDTO;
public class AgentTableCreator {
	public AgentTableCreator() {
		System.out.println("Creating or Checking HBase tables...");
	}
	public void createAgentTables(int agentHashCode) {
		long startTime=System.currentTimeMillis();
		CreateServerTable serverInfo=new CreateServerTable(HBASE_SERVER_TABLE);
		serverInfo.start();
		
		CreateJVMTable jvmInfo=new CreateJVMTable(HBASE_JVM_TABLE+"_"+agentHashCode);
		jvmInfo.start();
		
		CreateTPSTable tpsInfo=new CreateTPSTable(HBASE_TPS_TABLE+"_"+agentHashCode);
		tpsInfo.start();
		
		CreateRequestTable requestInfo=new CreateRequestTable(HBASE_REQUEST_TABLE+"_"+agentHashCode);
		requestInfo.start();
		
		CreateDatabaseTable queryInfo=new CreateDatabaseTable(HBASE_DATABASE_TABLE);
		queryInfo.start();
		
		try {
			serverInfo.join();
			jvmInfo.join();
			tpsInfo.join();
			requestInfo.join();
			queryInfo.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime=System.currentTimeMillis();
		double elapsedTime=(endTime-startTime)/1000.0;
		System.out.println("Created or Checking HBase tables in "+elapsedTime+" seconds.");
		
	}
	public void setAgentTable(AgentInfoDTO dto) {
		RequestTransactionDataManager manager=new RequestTransactionDataManager();
		manager.addAgent(dto.getHostHashCode(), dto.getHostIP(), dto.getPortNumbers(),dto.getAgentTCPPortNumber());
		if(TomcatProfilerReceiverConfig.USING_HBASE) {
//			deleteAllDB();
			createAgentTables(dto.getHostHashCode());
			
			PutServerData put=new PutServerData(HBASE_SERVER_TABLE,dto);
			DataPutThreadManager.execute(put);
		}
	}
	public void deleteAllDB() {
		try {
			System.out.println("Deleting all HBase DB");
			long startTime=System.currentTimeMillis();
			Configuration config = HBaseConfiguration.create();
			HBaseAdmin admin = new HBaseAdmin(config);
			HTableDescriptor desc[]=admin.listTables();
			for(HTableDescriptor tempDesc:desc) {
				byte[] tableName=tempDesc.getName();
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			}
			admin.close();
			long endTime=System.currentTimeMillis();
			System.out.println("Deleted all HBase DB in "+(endTime-startTime)+" ms" );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
