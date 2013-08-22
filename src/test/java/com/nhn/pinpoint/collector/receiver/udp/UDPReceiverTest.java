package com.nhn.pinpoint.collector.receiver.udp;

import java.net.DatagramSocket;
import java.net.SocketException;

import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.DataReceiver;
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

			DataReceiver receiver = context.getBean("udpReceiver", UDPReceiver.class);
			receiver.start();
            // start시점을 좀더 정확히 알수 있어야 될거 같음.
            // start한 다음에 바로 셧다운하니. receive thread에서 localaddress를 제대로 못찾는 문제가 있음.
            Thread.sleep(1000);

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
