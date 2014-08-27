package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import junit.framework.Assert;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.thrift.TBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 */
public class UdpDataSenderTest {
    @BeforeClass
    public static void before() {
        Slf4jLoggerBinderInitializer.beforeClass();
    }

    @AfterClass
    public static void after() {
        Slf4jLoggerBinderInitializer.afterClass();
    }



    @Test
    public void sendAndFlushChck() throws InterruptedException {
        UdpDataSender sender = new UdpDataSender("localhost", 9009, "test", 128, 1000, 1024*64*100);

        TAgentInfo agentInfo = new TAgentInfo();
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.stop();
    }

    @Test
    public void sendAndLarge() throws InterruptedException {
        String random = RandomStringUtils.randomAlphabetic(UdpDataSender.UDP_MAX_PACKET_LENGTH);
        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId(random);
        boolean limit = sendMessage_getLimit(agentInfo);
        Assert.assertTrue("limit overflow",limit);

        boolean noLimit = sendMessage_getLimit(new TAgentInfo());
        Assert.assertFalse("success", noLimit);


    }

    private boolean sendMessage_getLimit(TBase tbase) throws InterruptedException {
        final AtomicBoolean limitCounter = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        UdpDataSender sender = new UdpDataSender("localhost", 9009, "test", 128, 1000, 1024*64*100) {
            @Override
            protected boolean isLimit(int interBufferSize) {
                boolean limit = super.isLimit(interBufferSize);
                limitCounter.set(limit);
                latch.countDown();
                return limit;
            }
        };
        try {
            sender.send(tbase);
            latch.await(5000, TimeUnit.MILLISECONDS);
        } finally {
            sender.stop();
        }
        return limitCounter.get();
    }

}
