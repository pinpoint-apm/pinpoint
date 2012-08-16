package com.profiler.data.read;

import java.net.DatagramPacket;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.profiler.config.TomcatProfilerReceiverConfig;
import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.data.manager.RequestTransactionDataManager;
import com.profiler.data.store.hbase.put2.DataPutThreadManager;
import com.profiler.data.store.hbase.put2.PutJVMData;
import com.profiler.dto.JVMInfoThriftDTO;

public class ReadJVMData implements ReadHandler {
	private static final Logger logger = Logger.getLogger("com.profiler.data.read.ReadJVMData");
	
	long receiveTime=0;

	public ReadJVMData() {
	}

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        JVMInfoThriftDTO dto = (JVMInfoThriftDTO) tbase;
		try {
			int agentHashCode=dto.getAgentHashCode();
			
			RequestTransactionDataManager manager=new RequestTransactionDataManager();
			manager.addAgentHashCode(agentHashCode);
			
			checkAgentHashCodeIsExist(agentHashCode);
			logger.debug(dto.toString());
			
			if(TomcatProfilerReceiverConfig.USING_HBASE) {
				String tableName=TomcatProfilerReceiverConstant.HBASE_JVM_TABLE+"_"+agentHashCode;
				PutJVMData put=new PutJVMData(tableName,dto);
				DataPutThreadManager.execute(put);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void checkAgentHashCodeIsExist(int agentHashCode) {
		RequestTransactionDataManager manager=new RequestTransactionDataManager();
		if(manager.containsAgentInfo(agentHashCode)) {
//			System.out.println(ReceivedDataManager.getAgentVO(agentHashCode));
		} else {
//			System.out.println("No host data");
		}
	}
}
