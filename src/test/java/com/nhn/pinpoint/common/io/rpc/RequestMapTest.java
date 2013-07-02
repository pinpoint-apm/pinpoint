package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RequestMapTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testRegisterRequest() throws Exception {
        RequestMap requestMap = new RequestMap(10);
        try {
            RequestPacket packet = new RequestPacket(new byte[0]);
            MessageFuture messageFuture = requestMap.registerRequest(packet, 50);
            Thread.sleep(200);

            Assert.assertTrue(messageFuture.isReady());
            Assert.assertFalse(messageFuture.isSuccess());
            Assert.assertTrue(messageFuture.getCause().getMessage().contains("timeout"));
            logger.debug(messageFuture.getCause().getMessage());
        } finally {
            requestMap.close();
        }

    }

    @Test
    public void testRemoveMessageFuture() throws Exception {
        RequestMap requestMap = new RequestMap(10);
        try {
            RequestPacket packet = new RequestPacket(1, new byte[0]);
            MessageFuture messageFuture = requestMap.registerRequest(packet, 2000);

            messageFuture.setFailure(new RuntimeException());

            MessageFuture nullFuture = requestMap.removeMessageFuture(packet.getRequestId());
            Assert.assertNull(nullFuture);


        } finally {
            requestMap.close();
        }

    }

    @Test
    public void testClose() throws Exception {

    }
}
