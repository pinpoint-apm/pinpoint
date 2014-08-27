package com.nhn.pinpoint.collector.receiver.tcp;

import com.nhn.pinpoint.collector.receiver.UdpDispatchHandler;
import junit.framework.Assert;
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

    @Test
    public void l4ipList() throws UnknownHostException {
        String two = "10.118.202.30,10.118.202.31";
        String[] split = two.split(",");
        Assert.assertEquals(split.length, 2);

        // 뒤에 빈공간이 있으면 1인가 2인가?
        String twoEmpty = "10.118.202.30,";
        String[] splitEmpty = twoEmpty.split(",");
        Assert.assertEquals(splitEmpty.length, 1);

    }
}
