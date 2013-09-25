package com.nhn.pinpoint.profiler.monitor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.thrift.dto.AgentInfo;

public class AgentStatMonitorTest {

	@Test
	public void test() throws InterruptedException {
		System.setProperty("pinpoint.log", ".");
		TraceContext context = new DefaultTraceContext();
		AgentStatMonitor monitor = new AgentStatMonitor(context, null);
		AgentInfo info = new AgentInfo();
		info.setAgentId("agentId");
		monitor.setAgentInfo(info);
		
		monitor.setDataSender(new UdpDataSender("127.0.0.1", 12345, "udp-sender"));
		monitor.start();
		
		while (true) {
			Thread.sleep(1000000);
		}
	}

}
