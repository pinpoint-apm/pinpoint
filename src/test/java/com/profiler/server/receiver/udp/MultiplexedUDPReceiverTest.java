package com.profiler.server.receiver.udp;

import java.util.concurrent.Future;

import com.nhn.pinpoint.server.receiver.udp.DataReceiver;
import com.nhn.pinpoint.server.receiver.udp.MultiplexedUDPReceiver;
import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import com.nhn.pinpoint.server.spring.ApplicationContextUtils;

public class MultiplexedUDPReceiverTest {
	@Test
	public void startStop() {
		try {
			GenericApplicationContext context = ApplicationContextUtils.createContext();
			DataReceiver receiver = new MultiplexedUDPReceiver(context);
			Future<Boolean> startLatch = receiver.start();

			startLatch.get();

			receiver.shutdown();
			context.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
