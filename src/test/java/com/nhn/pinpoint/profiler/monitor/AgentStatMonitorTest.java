package com.nhn.pinpoint.profiler.monitor;

import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import org.junit.Test;

public class AgentStatMonitorTest {


	@Test
	public void test() throws InterruptedException {

		System.setProperty("pinpoint.log", "test.");
		AgentStatMonitor monitor = new AgentStatMonitor(new LoggingDataSender(), "agentId", System.currentTimeMillis());

		monitor.start();

        Thread.sleep(100);

        monitor.stop();

	}

}
