/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.util.Timer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * @author Taejin Koo
 */
public class HealthCheckManagerTest {

    private static Timer timer;
    private static HealthCheckManager pingPacketSender;

    @BeforeClass
    public static void setUp() {
        timer = TimerFactory.createHashedWheelTimer("PingPacketSenderTestTimer", 50, TimeUnit.MILLISECONDS, 512);

        pingPacketSender = new HealthCheckManager(timer, 3000);
        pingPacketSender.start(1000);
    }

    @AfterClass
    public static void tearDown() {
        if (pingPacketSender != null) {
            pingPacketSender.stop();
        }
        if (timer != null) {
            timer.stop();
        }
    }

    @Test
    public void legacyPingPacketTest() throws Exception {
        Channel mockChannel = mock(Channel.class);
        ChannelFuture mockChannelFuture = mock(ChannelFuture.class);
        when(mockChannel.getCloseFuture()).thenReturn(mockChannelFuture);
        when(mockChannel.write(PingPacket.PING_PACKET)).thenReturn(mockChannelFuture);

        try {
            pingPacketSender.receivedPingAndStartPingSend(mockChannel, PingPacket.PING_PACKET);
            verify(mockChannel, timeout(3000).atLeastOnce()).write(PingPacket.PING_PACKET);
        } finally {
            pingPacketSender.removeChannel(mockChannel);
        }
    }

    @Test
    public void pingPacketTest() throws Exception {
        Channel mockChannel = mock(Channel.class);
        ChannelFuture mockChannelFuture = mock(ChannelFuture.class);
        when(mockChannel.getCloseFuture()).thenReturn(mockChannelFuture);
        when(mockChannel.write(PingSimplePacket.PING_PACKET)).thenReturn(mockChannelFuture);

        try {
            pingPacketSender.receivedPingAndStartPingSend(mockChannel, PingSimplePacket.PING_PACKET);
            verify(mockChannel, timeout(3000).atLeastOnce()).write(PingSimplePacket.PING_PACKET);
        } finally {
            pingPacketSender.removeChannel(mockChannel);
        }
    }

    @Test
    public void withoutPacketTest() throws Exception {
        Channel mockChannel = mock(Channel.class);
        ChannelFuture mockChannelFuture = mock(ChannelFuture.class);
        when(mockChannel.getCloseFuture()).thenReturn(mockChannelFuture);
        when(mockChannel.write(PingSimplePacket.PING_PACKET)).thenReturn(mockChannelFuture);

        try {
            pingPacketSender.registerWaitingToReceivePing(mockChannel);
            verify(mockChannel, timeout(5000).atLeastOnce()).close();
        } finally {
            pingPacketSender.removeChannel(mockChannel);
        }
    }

}
