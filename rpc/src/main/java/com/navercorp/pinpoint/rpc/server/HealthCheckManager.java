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
import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class HealthCheckManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final PingSimplePacket PING_PACKET = PingSimplePacket.PING_PACKET;
    private static final PingPacket LEGACY_PING_PACKET = PingPacket.PING_PACKET;

    private static final long MAXIMUM_WAITING_TIME_MILLIS = 30 * 60 * 1000;

    private volatile boolean startMethodInvoked = false;
    private volatile boolean isStopped = false;

    private final Timer timer;
    private final long waitTimeMillis;

    private final ChannelGroup channelGroup;

    private final WriteFailFutureListener writeFailListener = new WriteFailFutureListener(logger, "ping write fail.", "ping write success.");

    public HealthCheckManager(Timer healthCheckTimer, ChannelGroup channelGroup) {
        this(healthCheckTimer, MAXIMUM_WAITING_TIME_MILLIS, channelGroup);
    }

    public HealthCheckManager(Timer timer, long waitTimeMillis, ChannelGroup channelGroup) {
        Assert.requireNonNull(timer, "timer must not be null");
        Assert.isTrue(waitTimeMillis > 0, "waitTimeMillis is must greater than 0");
        Assert.requireNonNull(channelGroup, "channelGroup must not be null");

        this.timer = timer;
        this.waitTimeMillis = waitTimeMillis;

        this.channelGroup = channelGroup;
    }

    // no guarantee of synchronization
    public void start(long intervalMillis) {
        Assert.isTrue(intervalMillis > 0, "intervalMillis is must be greater than zero");

        logger.debug("start() started");
        if (isStopped) {
            logger.warn("start() failed. already stopped");
            return;
        }

        if (startMethodInvoked) {
            logger.warn("start() failed. already invoked");
            return;
        }

        startMethodInvoked = true;
        registerTask(new HealthCheckTask(intervalMillis));
    }

    public void stop() {
        logger.debug("stop() started");
        if (!isStopped) {
            isStopped = true;
        }
    }


    private void registerTask(HealthCheckTask task) {
        try {
            logger.debug("registerTask() started");
            timer.newTimeout(task, task.getIntervalMillis(), TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            // stop in case of timer stopped
            logger.debug("timer stopped. Caused:{}", e.getMessage());
        }
    }

    private PinpointServer getPinpointServer(Channel channel) {
        if (channel == null) {
            return null;
        }
        if (!channel.isConnected()) {
            return null;
        }

        Object attachment = channel.getAttachment();
        if (attachment instanceof PinpointServer) {
            return (PinpointServer) attachment;
        } else {
            return null;
        }
    }

    private boolean hasExpiredReceivingPing(PinpointServer pinpointServer) {
        if (pinpointServer.getHealthCheckState() != HealthCheckState.WAIT) {
            return false;
        }

        long waitStartTimestamp = pinpointServer.getStartTimestamp();
        return System.currentTimeMillis() > waitStartTimestamp + waitTimeMillis;
    }

    private class HealthCheckTask implements TimerTask {

        private final long intervalMillis;

        public HealthCheckTask(long intervalMillis) {
            this.intervalMillis = intervalMillis;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (isStopped) {
                return;
            }

            if (timeout.isCancelled()) {
                registerTask(this);
                return;
            }

            for (Channel channel : channelGroup) {
                PinpointServer pinpointServer = getPinpointServer(channel);
                if (pinpointServer == null) {
                    continue;
                }

                HealthCheckState healthCheckState = pinpointServer.getHealthCheckState();
                switch (healthCheckState) {
                    case RECEIVED:
                        if (isDebug) {
                            logger.debug("ping write. channel:{}, packet:{}.", channel, PING_PACKET);
                        }
                        channel.write(PING_PACKET).addListener(writeFailListener);
                        break;
                    case RECEIVED_LEGACY:
                        if (isDebug) {
                            logger.debug("ping write. channel:{}, packet:{}.", channel, LEGACY_PING_PACKET);
                        }
                        channel.write(LEGACY_PING_PACKET).addListener(writeFailListener);
                        break;
                    case WAIT:
                        if (hasExpiredReceivingPing(pinpointServer)) {
                            logger.warn("expired while waiting to receive ping. channel:{} will be closed", channel);
                            channel.close();
                        }
                        break;
                }
            }

            if (!isStopped) {
                registerTask(this);
            }
        }

        private long getIntervalMillis() {
            return intervalMillis;
        }

    }

}
