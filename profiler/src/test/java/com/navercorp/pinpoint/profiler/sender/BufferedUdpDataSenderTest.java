package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.sender.BufferedUdpDataSender;

import junit.framework.Assert;

import org.junit.Test;


public class BufferedUdpDataSenderTest {

    @Test
    public void testSendPacket() throws Exception {



    }

    @Test
    public void testStop_StopFlushThread() throws Exception {

        final BufferedUdpDataSender sender = new BufferedUdpDataSender("localhost", 9999, "testUdpSender", 100);

        final String flushThreadName = sender.getFlushThreadName();

        Assert.assertTrue(ThreadMXBeanUtils.findThreadName(flushThreadName));

        sender.stop();

        Assert.assertFalse(ThreadMXBeanUtils.findThreadName(flushThreadName));
        // ?? finally { send.stop() }
    }
}