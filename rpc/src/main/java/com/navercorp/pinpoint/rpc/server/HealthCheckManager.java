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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.common.ScheduledPacketSender;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class HealthCheckManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final PingSimplePacket PING_PACKET = PingSimplePacket.PING_PACKET;
    private static final PingPacket LEGACY_PING_PACKET = PingPacket.PING_PACKET;

    private static final long MAXIMUM_WAITING_TIME_MILLIS = 30 * 60 * 1000;

    private final Object lock = new Object();
    private final long waitingHealthCheckTimeMillis;

    private final List<Channel> waitingToReceivePingChannelList = new Vector<Channel>();
    private final ChannelGroup legacyPingChannelGroup = new DefaultChannelGroup("legacyPingChannelGroup");
    private final ChannelGroup pingChannelGroup = new DefaultChannelGroup("pingChannelGroup");
    private final List<ScheduledPacketSender> pingPacketSenderList;

    private final Timer healthCheckTimer;
    private boolean startMethodInvoked = false;
    private boolean isStopped = false;

    public HealthCheckManager(Timer healthCheckTimer) {
        this(healthCheckTimer, MAXIMUM_WAITING_TIME_MILLIS);
    }

    public HealthCheckManager(Timer healthCheckTimer, long waitingHealthCheckTimeMillis) {
        Assert.requireNonNull(healthCheckTimer, "healthCheckTimer may not be null");

        ScheduledPacketSender legacyPingPacketSender = new ScheduledPacketSender(healthCheckTimer, legacyPingChannelGroup, LEGACY_PING_PACKET);
        ScheduledPacketSender pingPacketSender = new ScheduledPacketSender(healthCheckTimer, pingChannelGroup, PING_PACKET);

        List<ScheduledPacketSender> pingPacketSenderList = new ArrayList<ScheduledPacketSender>(2);
        pingPacketSenderList.add(legacyPingPacketSender);
        pingPacketSenderList.add(pingPacketSender);

        this.pingPacketSenderList = Collections.unmodifiableList(pingPacketSenderList);
        this.healthCheckTimer = healthCheckTimer;
        this.waitingHealthCheckTimeMillis = waitingHealthCheckTimeMillis;
    }

    public void start(long intervalMillis) {
        logger.debug("start() started");
        synchronized (lock) {
            if (isStopped) {
                logger.warn("start() failed. already stopped");
                return;
            }

            if (startMethodInvoked) {
                logger.warn("start() failed. already invoked");
                return;
            }

            startMethodInvoked = true;
            for (ScheduledPacketSender pingSender : pingPacketSenderList) {
                pingSender.start(intervalMillis);
            }
        }
    }

    public void stop() {
        logger.debug("stop() started");
        synchronized (lock) {
            if (!isStopped) {
                isStopped = true;

                for (ScheduledPacketSender pingSender : pingPacketSenderList) {
                    pingSender.stop();
                }
            }
        }
    }

    public boolean registerWaitingToReceivePing(Channel channel) {
        Assert.requireNonNull(channel, "channel may not be null");

        logger.debug("registerWaitingToReceivePing() started. channel:{}", channel);
        synchronized (lock) {
            if (isStopped) {
                logger.warn("registerWaitingToReceivePing() failed. already stopped");
                return false;
            }

            if (legacyPingChannelGroup.contains(channel) || pingChannelGroup.contains(channel)) {
                return false;
            }

            if (waitingToReceivePingChannelList.contains(channel)) {
                return false;
            }

            waitingToReceivePingChannelList.add(channel);

            ExpiredWaitingHealthCheckTimerTask expiredWaitingHealthCheckTimerTask = new ExpiredWaitingHealthCheckTimerTask(channel);
            healthCheckTimer.newTimeout(expiredWaitingHealthCheckTimerTask, waitingHealthCheckTimeMillis, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    public boolean receivedPingAndStartPingSend(Channel channel, Packet pingPacket) {
        Assert.requireNonNull(channel, "channel may not be null");
        Assert.requireNonNull(pingPacket, "pingPacket may not be null");

        logger.debug("receivedPingAndStartPingSend() started. channel:{}, packet:{}", channel, pingPacket);

        if (pingPacket != PING_PACKET && pingPacket != LEGACY_PING_PACKET) {
            logger.warn("Illegal pingPacket. packet:{}", pingPacket);
            return false;
        }

        synchronized (lock) {
            if (isStopped) {
                logger.warn("receivedPingAndStartPingSend() failed. already stopped");
                return false;
            }

            if (legacyPingChannelGroup.contains(channel) || pingChannelGroup.contains(channel)) {
                return false;
            }

            waitingToReceivePingChannelList.remove(channel);

            if (pingPacket == PING_PACKET) {
                return pingChannelGroup.add(channel);
            } else if (pingPacket == LEGACY_PING_PACKET) {
                return legacyPingChannelGroup.add(channel);
            } else {
                return false;
            }
        }
    }

    public boolean removeChannel(Channel channel) {
        Assert.requireNonNull(channel, "channel may not be null");

        logger.debug("removeChannel() started channel:{}", channel);

        synchronized (lock) {
            if (isStopped) {
                logger.warn("removeChannel() failed. already stopped");
                return false;
            }

            waitingToReceivePingChannelList.remove(channel);

            if (legacyPingChannelGroup.contains(channel)) {
                return legacyPingChannelGroup.remove(channel);
            }
            if (pingChannelGroup.contains(channel)) {
                return pingChannelGroup.remove(channel);
            }
            return false;
        }
    }

    private class ExpiredWaitingHealthCheckTimerTask implements TimerTask {

        private final Channel channel;

        public ExpiredWaitingHealthCheckTimerTask(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled()) {
                return;
            }

            if (isStopped) {
                return;
            }


            if (waitingToReceivePingChannelList.contains(channel)) {
                logger.warn("expired while waiting to receive ping. channel:{} will be closed", channel);
                removeChannel(channel);
                channel.close();
            }
        }

    }

}
