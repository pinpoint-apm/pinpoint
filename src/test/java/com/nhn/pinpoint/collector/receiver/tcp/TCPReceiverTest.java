package com.nhn.pinpoint.collector.receiver.tcp;

import com.nhn.pinpoint.collector.receiver.UdpDispatchHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author emeroad
 */
public class TCPReceiverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void server() throws InterruptedException {
        TCPReceiver tcpReceiver = new TCPReceiver(new UdpDispatchHandler(), "0.0.0.0", 9099);
        tcpReceiver.start();
        Thread.sleep(1000);
        tcpReceiver.stop();
    }

    @Test
    public void l4ip() throws UnknownHostException {
        InetAddress byName = InetAddress.getByName("10.118.202.30");
        logger.debug("byName:{}", byName);
    }
}
