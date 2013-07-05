package com.nhn.pinpoint.collector.receiver.udp;

import java.util.concurrent.Future;

import com.nhn.pinpoint.collector.config.TomcatProfilerReceiverConfig;
import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import com.nhn.pinpoint.collector.spring.ApplicationContextUtils;

public class MultiplexedUDPReceiverTest {
	@Test
	public void startStop() {
		try {
			GenericApplicationContext context = ApplicationContextUtils.createContext();
            MultiplexedPacketHandler multiplexedPacketHandler = ApplicationContextUtils.getMultiplexedPacketHandler(context);

            // local에서 기본포트로 테스트 하면 포트 출돌로 에러남.
			DataReceiver receiver = new MultiplexedUDPReceiver(multiplexedPacketHandler, TomcatProfilerReceiverConfig.SERVER_UDP_LISTEN_PORT+10);
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
