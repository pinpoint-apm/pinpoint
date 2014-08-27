package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import org.jboss.netty.util.HashedWheelTimer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class RequestManagerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testRegisterRequest() throws Exception {
        HashedWheelTimer timer = getTimer();
        RequestManager requestManager = new RequestManager(timer);
        try {
            RequestPacket packet = new RequestPacket(new byte[0]);
            Future future = requestManager.register(packet, 50);
            Thread.sleep(200);

            Assert.assertTrue(future.isReady());
            Assert.assertFalse(future.isSuccess());
            Assert.assertTrue(future.getCause().getMessage().contains("timeout"));
            logger.debug(future.getCause().getMessage());
        } finally {
            requestManager.close();
            timer.stop();
        }
    }

    @Test
    public void testRemoveMessageFuture() throws Exception {
        HashedWheelTimer timer = getTimer();
        RequestManager requestManager = new RequestManager(timer);
        try {
            RequestPacket packet = new RequestPacket(1, new byte[0]);
            DefaultFuture future = requestManager.register(packet, 2000);

            future.setFailure(new RuntimeException());

            Future nullFuture = requestManager.removeMessageFuture(packet.getRequestId());
            Assert.assertNull(nullFuture);


        } finally {
            requestManager.close();
            timer.stop();
        }

    }

    private HashedWheelTimer getTimer() {
        return new HashedWheelTimer(10, TimeUnit.MICROSECONDS);
    }

    //    @Test
    public void testTimerStartTiming() throws InterruptedException {
        HashedWheelTimer timer = new HashedWheelTimer(1000, TimeUnit.MILLISECONDS);
        timer.start();
        // start해야 타이머가 thread가 동작한다.
        timer.stop();
    }

    @Test
    public void testClose() throws Exception {

    }
}
