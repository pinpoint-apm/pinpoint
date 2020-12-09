package com.navercorp.pinpoint.collector.receiver.grpc.channelz;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;

public class DefaultChannelzRegistryTest {
    @Test
    public void testAddSocket() {
        InetSocketAddress remote = InetSocketAddress.createUnresolved("1.1.1.1", 80);
        InetSocketAddress local = InetSocketAddress.createUnresolved("127.0.0.1", 9991);
        long logId = 1;

        DefaultChannelzRegistry registry = new DefaultChannelzRegistry();
        registry.addSocket(logId, remote, local);

        Long removedLogId = registry.removeSocket(remote);
        Assert.assertEquals(logId, removedLogId.longValue());
    }

    @Test
    public void testAddSocket_multiple() {
        InetSocketAddress remote1 = InetSocketAddress.createUnresolved("1.1.1.1", 80);
        InetSocketAddress local1 = InetSocketAddress.createUnresolved("127.0.0.1", 9991);
        long logId1 = 1;

        InetSocketAddress remote2 = InetSocketAddress.createUnresolved("2.2.2.2", 90);
        InetSocketAddress local2 = InetSocketAddress.createUnresolved("127.0.0.1", 19991);
        long logId2 = 2;

        DefaultChannelzRegistry registry = new DefaultChannelzRegistry();
        registry.addSocket(logId1, remote1, local1);
        registry.addSocket(logId2, remote2, local2);

        Long removedLogId = registry.removeSocket(remote1);
        Assert.assertEquals(logId1, removedLogId.longValue());

        Assert.assertEquals(-1L, registry.removeSocket(remote1).longValue());
    }

    @Test
    public void testMemoryleak() {
        InetSocketAddress remote1 = InetSocketAddress.createUnresolved("1.1.1.1", 80);
        InetSocketAddress local1 = InetSocketAddress.createUnresolved("127.0.0.1", 9991);
        long logId1 = 1;


        DefaultChannelzRegistry registry = new DefaultChannelzRegistry();
        registry.addSocket(logId1, remote1, local1);
        long removedLogId = registry.removeSocket(remote1);

        Assert.assertEquals(logId1, removedLogId);

        Assert.assertEquals(0, registry.getRemoteAddressSocketMapSize());
        Assert.assertEquals(0, registry.getSocketMapSize());
    }

    @Test
    public void testMemoryleak2() {
        InetSocketAddress remote1 = InetSocketAddress.createUnresolved("1.1.1.1", 80);
        InetSocketAddress local1 = InetSocketAddress.createUnresolved("127.0.0.1", 9991);

        DefaultChannelzRegistry registry = new DefaultChannelzRegistry();
        registry.addSocket(1, remote1, local1);

        InetSocketAddress unkonwn = InetSocketAddress.createUnresolved("2.2.2.2", 9991);
        Assert.assertEquals(-1L, registry.removeSocket(unkonwn).longValue());
    }
}