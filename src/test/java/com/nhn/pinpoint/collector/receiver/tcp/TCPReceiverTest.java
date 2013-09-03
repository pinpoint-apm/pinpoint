package com.nhn.pinpoint.collector.receiver.tcp;

import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import org.junit.Test;

/**
 *
 */
public class TCPReceiverTest {
    @Test
    public void server() throws InterruptedException {
        TCPReceiver tcpReceiver = new TCPReceiver(new DispatchHandler(), 11111);
        tcpReceiver.start();
        Thread.sleep(1000);
        tcpReceiver.stop();
    }
}
