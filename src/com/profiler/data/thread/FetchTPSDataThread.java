package com.profiler.data.thread;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.profiler.config.TomcatProfilerReceiverConstant;
import com.profiler.data.manager.RequestTransactionDataManager;

public class FetchTPSDataThread extends Thread{
	private static final Logger logger = Logger.getLogger("TPSInfo");
	public FetchTPSDataThread() {
		
	}
	public void run() {
		long gap=0;
		RequestTransactionDataManager manager=new RequestTransactionDataManager();
		while(true) {
			try {
				if(gap<=1000) {
					long currentMod=System.currentTimeMillis()%1000;
					Thread.sleep(1000-gap-currentMod+1);
				} else {
					logger.debug("Data processing time elapsed :"+gap+" ms");
				}
				long startTime=System.currentTimeMillis();
				HashSet<Integer> agentSet=manager.getAgentSet();
				long checkTime=startTime-TomcatProfilerReceiverConstant.DATA_FETCH_INTERVAL;
				StringBuilder printData=new StringBuilder();
				printData.append("Gap=");
				for(Integer tempAgent:agentSet) {
					String key=manager.getTimeKey(tempAgent,checkTime);
					int reqPS=manager.getReqPerSecond(key);
					int resPS=manager.getResPerSecond(key);
					printData.append("[");
					printData.append(tempAgent);
					printData.append(" Req=").append(reqPS);
					printData.append(" Res=").append(resPS);
					printData.append("] ");
					manager.saveRequestAndResponseData(tempAgent,key);
					manager.saveTPSData(tempAgent,reqPS,resPS,checkTime);
				}
				long endTime=System.currentTimeMillis();
				gap=endTime-startTime;
				printData.insert(4, gap);
				logger.debug(printData);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
