package com.nhn.pinpoint.collector.receiver.udp;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Future;

import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import com.nhn.pinpoint.collector.spring.ApplicationContextUtils;

public class UDPReceiverTest {
	@Test
	public void startStop() {
		try {
			GenericApplicationContext context = ApplicationContextUtils.createContext();
            DispatchHandler multiplexedPacketHandler = ApplicationContextUtils.getDispatchHandler(context);

            // local에서 기본포트로 테스트 하면 포트 출돌로 에러남.
            CollectorConfiguration config = new CollectorConfiguration();
			DataReceiver receiver = new UDPReceiver(multiplexedPacketHandler, config.getCollectorUdpListenPort() +10);
			receiver.start();


			receiver.shutdown();
			context.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

    @Test
    public void socketBufferSize() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        int receiveBufferSize = datagramSocket.getReceiveBufferSize();
        System.out.println(receiveBufferSize);
        datagramSocket.setReceiveBufferSize(64*1024*10);
        System.out.println(datagramSocket.getReceiveBufferSize());
    }
}
