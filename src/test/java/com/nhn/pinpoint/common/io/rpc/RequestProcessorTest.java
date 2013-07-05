package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RequestProcessorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testRegisterRequest() throws Exception {
        RequestProcessor requestProcessor = new RequestProcessor(10);
        try {
            RequestPacket packet = new RequestPacket(new byte[0]);
            Future future = requestProcessor.registerRequest(packet, 50);
            Thread.sleep(200);

            Assert.assertTrue(future.isReady());
            Assert.assertFalse(future.isSuccess());
            Assert.assertTrue(future.getCause().getMessage().contains("timeout"));
            logger.debug(future.getCause().getMessage());
        } finally {
            requestProcessor.close();
        }

    }

    @Test
    public void testRemoveMessageFuture() throws Exception {
        RequestProcessor requestProcessor = new RequestProcessor(10);
        try {
            RequestPacket packet = new RequestPacket(1, new byte[0]);
            DefaultFuture future = requestProcessor.registerRequest(packet, 2000);

            future.setFailure(new RuntimeException());

            Future nullFuture = requestProcessor.removeMessageFuture(packet.getRequestId());
            Assert.assertNull(nullFuture);


        } finally {
            requestProcessor.close();
        }

    }

    @Test
    public void testClose() throws Exception {

    }
}
