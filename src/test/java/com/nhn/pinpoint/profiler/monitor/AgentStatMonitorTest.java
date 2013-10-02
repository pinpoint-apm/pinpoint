package com.nhn.pinpoint.profiler.monitor;

import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import org.junit.Test;


import com.nhn.pinpoint.thrift.dto.AgentInfo;

public class AgentStatMonitorTest {


	@Test
	public void test() throws InterruptedException {
		System.setProperty("pinpoint.log", ".");
		AgentStatMonitor monitor = new AgentStatMonitor(new LoggingDataSender());
		AgentInfo info = new AgentInfo();
		info.setAgentId("agentId");
		monitor.setAgentInfo(info);
		monitor.start();

        Thread.sleep(100);

        monitor.stop();

	}

}
