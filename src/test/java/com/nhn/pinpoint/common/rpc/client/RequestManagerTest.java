package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.DefaultFuture;
import com.nhn.pinpoint.common.rpc.Future;
import com.nhn.pinpoint.common.rpc.packet.RequestPacket;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RequestManagerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testRegisterRequest() throws Exception {
        RequestManager requestManager = new RequestManager(10);
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
        }

    }

    @Test
    public void testRemoveMessageFuture() throws Exception {
        RequestManager requestManager = new RequestManager(10);
        try {
            RequestPacket packet = new RequestPacket(1, new byte[0]);
            DefaultFuture future = requestManager.register(packet, 2000);

            future.setFailure(new RuntimeException());

            Future nullFuture = requestManager.removeMessageFuture(packet.getRequestId());
            Assert.assertNull(nullFuture);


        } finally {
            requestManager.close();
        }

    }

    @Test
    public void testClose() throws Exception {

    }
}
