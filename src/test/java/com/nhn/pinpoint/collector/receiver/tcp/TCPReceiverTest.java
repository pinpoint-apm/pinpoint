package com.nhn.pinpoint.collector.receiver.tcp;

import com.nhn.pinpoint.collector.receiver.UdpDispatchHandler;
import org.junit.Test;

/**
 * @author emeroad
 */
public class TCPReceiverTest {
    @Test
    public void server() throws InterruptedException {
        TCPReceiver tcpReceiver = new TCPReceiver(new UdpDispatchHandler(), "0.0.0.0", 9099);
        tcpReceiver.start();
        Thread.sleep(1000);
        tcpReceiver.stop();
    }
}
