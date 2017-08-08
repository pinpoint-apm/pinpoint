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
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
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

    @BeforeClass
    public static void setUp() {
        timer = TimerFactory.createHashedWheelTimer("PingPacketSenderTestTimer", 50, TimeUnit.MILLISECONDS, 512);
    }

    @AfterClass
    public static void tearDown() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Test
    public void legacyPingPacketTest() throws Exception {
        ChannelGroup channelGroup = new DefaultChannelGroup();

        HealthCheckManager healthCheckManager = new HealthCheckManager(timer, 3000, channelGroup);
        healthCheckManager.start(1000);

        Channel mockChannel = createMockChannel(HealthCheckState.RECEIVED_LEGACY);
        channelGroup.add(mockChannel);
        try {
            verify(mockChannel, timeout(3000).atLeastOnce()).write(PingPacket.PING_PACKET);
        } finally {
            healthCheckManager.stop();
        }
    }

    @Test
    public void pingPacketTest() throws Exception {
        ChannelGroup channelGroup = new DefaultChannelGroup();

        HealthCheckManager healthCheckManager = new HealthCheckManager(timer, 3000, channelGroup);
        healthCheckManager.start(1000);

        Channel mockChannel = createMockChannel(HealthCheckState.RECEIVED);
        channelGroup.add(mockChannel);
        try {
            verify(mockChannel, timeout(3000).atLeastOnce()).write(PingSimplePacket.PING_PACKET);
        } finally {
            healthCheckManager.stop();
        }
    }

    @Test
    public void withoutPacketTest() throws Exception {
        ChannelGroup channelGroup = new DefaultChannelGroup();

        HealthCheckManager healthCheckManager = new HealthCheckManager(timer, 3000, channelGroup);
        healthCheckManager.start(1000);

        Channel mockChannel = createMockChannel(HealthCheckState.WAIT);
        channelGroup.add(mockChannel);

        try {
            verify(mockChannel, timeout(5000).atLeastOnce()).close();
        } finally {
            healthCheckManager.stop();
        }
    }

    private Channel createMockChannel(HealthCheckState state) {
        Channel mockChannel = mock(Channel.class);
        when(mockChannel.isConnected()).thenReturn(true);

        ChannelFuture mockChannelFuture = mock(ChannelFuture.class);
        when(mockChannel.write(PingPacket.PING_PACKET)).thenReturn(mockChannelFuture);
        when(mockChannel.write(PingSimplePacket.PING_PACKET)).thenReturn(mockChannelFuture);
        when(mockChannel.getCloseFuture()).thenReturn(mockChannelFuture);

        PinpointServer pinpointServer = mock(PinpointServer.class);
        when(pinpointServer.getHealthCheckState()).thenReturn(state);
        when(mockChannel.getAttachment()).thenReturn(pinpointServer);

        return mockChannel;
    }

}
