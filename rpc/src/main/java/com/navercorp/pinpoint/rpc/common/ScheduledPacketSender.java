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

package com.navercorp.pinpoint.rpc.common;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.packet.Packet;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ScheduledPacketSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Timer timer;

    private final ChannelGroup channelGroup;
    private final WriteFailFutureListener writeFailListener;

    private final Packet packet;

    private boolean startMethodInvoked = false;
    private volatile boolean isStopped = false;

    public ScheduledPacketSender(Timer timer, ChannelGroup channelGroup, Packet packet) {
        this(timer, channelGroup, packet, packet.toString());
    }

    public ScheduledPacketSender(Timer timer, ChannelGroup channelGroup, Packet packet, String packetDescription) {
        Assert.requireNonNull(timer, "timer may not be null");
        Assert.requireNonNull(channelGroup, "channelGroup may not be null");
        Assert.requireNonNull(packet, "packet may not be null");
        Assert.requireNonNull(packetDescription, "packetDescription may not be null");

        this.timer = timer;

        this.channelGroup = channelGroup;
        this.writeFailListener = new WriteFailFutureListener(logger, packetDescription + " write fail", packetDescription + " write success");

        this.packet = packet;
    }

    /**
     * no guarantee of synchronization
     */
    public void start(long intervalMillis) {
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

        registerTask(new ScheduledPacketSenderTask(intervalMillis));
    }

    public void stop() {
        logger.debug("stop() started");
        isStopped = true;
    }

    private void registerTask(ScheduledPacketSenderTask task) {
        try {
            logger.debug("registerTask() started");
            timer.newTimeout(task, task.getIntervalMillis(), TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            // stop in case of timer stopped
            logger.debug("timer stopped. Caused:{}", e.getMessage());
        }
    }

    private class ScheduledPacketSenderTask implements TimerTask {

        private final long intervalMillis;

        public ScheduledPacketSenderTask(long intervalMillis) {
            Assert.isTrue(intervalMillis > 0, "Illegal intervalMillis");

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

            if (channelGroup != null && channelGroup.size() != 0) {
                final ChannelGroupFuture write = channelGroup.write(packet);
                if (logger.isWarnEnabled()) {
                    write.addListener(new ChannelGroupFutureListener() {
                        @Override
                        public void operationComplete(ChannelGroupFuture future) throws Exception {
                            if (logger.isWarnEnabled()) {
                                for (ChannelFuture channelFuture : future) {
                                    channelFuture.addListener(writeFailListener);
                                }
                            }
                        }
                    });
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
