/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class AgentInfoSender {
    // refresh daily
    private static final long DEFAULT_AGENT_INFO_REFRESH_INTERVAL_MS = 24 * 60 * 60 * 1000L;
    // retry every 3 seconds
    private static final long DEFAULT_AGENT_INFO_SEND_INTERVAL_MS = 3 * 1000L;
    // retry 3 times per attempt
    private static final int DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT = 3;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ThreadFactory threadFactory = new PinpointThreadFactory("Pinpoint-agentInfo-sender", true);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);

    private final EnhancedDataSender dataSender;
    private final AgentInfoFactory agentInfoFactory;
    private final long refreshIntervalMs;
    private final long sendIntervalMs;
    private final int maxTryPerAttempt;

    private AgentInfoSender(Builder builder) {
        this.dataSender = builder.dataSender;
        this.agentInfoFactory = builder.agentInfoFactory;
        this.refreshIntervalMs = builder.refreshIntervalMs;
        this.sendIntervalMs = builder.sendIntervalMs;
        this.maxTryPerAttempt = builder.maxTryPerAttempt;
    }

    public void start() {
        sendAgentInfo(Integer.MAX_VALUE);
        this.executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, this.refreshIntervalMs, this.refreshIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("AgentInfoSender stopped");
    }

    public void refresh() {
        sendAgentInfo(this.maxTryPerAttempt);
    }

    private void sendAgentInfo(int maxTries) {
        TAgentInfo agentInfo = agentInfoFactory.createAgentInfo();
        AgentInfoSendRunnable agentInfoSendRunnable = new AgentInfoSendRunnable(agentInfo);
        AgentInfoSendRunnableWrapper wrapper = new AgentInfoSendRunnableWrapper(agentInfoSendRunnable, maxTries);
        wrapper.repeatWithFixedDelay(this.executor, 0, this.sendIntervalMs, TimeUnit.MILLISECONDS);
    }

    private static class AgentInfoSendRunnableWrapper implements Runnable {
        private final AgentInfoSendRunnable delegate;
        private final int maxTryCount;
        private final AtomicInteger tryCount = new AtomicInteger();
        private volatile ScheduledFuture<?> self;

        private AgentInfoSendRunnableWrapper(AgentInfoSendRunnable agentInfoSendRunnable, int maxTryCount) {
            if (agentInfoSendRunnable == null) {
                throw new NullPointerException("agentInfoSendRunnable must not be null");
            }
            if (maxTryCount < 0) {
                throw new IllegalArgumentException("maxTryCount must not be less than 0");
            }
            this.delegate = agentInfoSendRunnable;
            this.maxTryCount = maxTryCount;
        }

        @Override
        public void run() {
            // Cancel self when delegated runnable is completed successfully, or when max try count has been reached
            if (this.delegate.isSuccessful() || this.tryCount.getAndIncrement() == this.maxTryCount) {
                this.self.cancel(false);
            } else {
                this.delegate.run();
            }
        }

        private void repeatWithFixedDelay(ScheduledExecutorService scheduledExecutorService, long initialDelay, long delay, TimeUnit unit) {
            this.self = scheduledExecutorService.scheduleWithFixedDelay(this, initialDelay, delay, unit);
        }
    }

    private class AgentInfoSendRunnable implements Runnable {
        private final AtomicBoolean isSuccessful = new AtomicBoolean(false);
        private final AgentInfoSenderListener agentInfoSenderListener = new AgentInfoSenderListener(this.isSuccessful);
        private final TAgentInfo agentInfo;

        private AgentInfoSendRunnable(TAgentInfo agentInfo) {
            this.agentInfo = agentInfo;
        }

        @Override
        public void run() {
            if (!isSuccessful.get()) {
                logger.info("Sending AgentInfo {}", agentInfo);
                dataSender.request(this.agentInfo, this.agentInfoSenderListener);
            }
        }

        public boolean isSuccessful() {
            return this.isSuccessful.get();
        }
    }

    public static class Builder {
        private final EnhancedDataSender dataSender;
        private final AgentInfoFactory agentInfoFactory;
        private long refreshIntervalMs = DEFAULT_AGENT_INFO_REFRESH_INTERVAL_MS;
        private long sendIntervalMs = DEFAULT_AGENT_INFO_SEND_INTERVAL_MS;
        private int maxTryPerAttempt = DEFAULT_MAX_TRY_COUNT_PER_ATTEMPT;

        public Builder(EnhancedDataSender dataSender, AgentInfoFactory agentInfoFactory) {
            if (dataSender == null) {
                throw new NullPointerException("enhancedDataSender must not be null");
            }
            if (agentInfoFactory == null) {
                throw new NullPointerException("agentInfoFactory must not be null");
            }
            this.dataSender = dataSender;
            this.agentInfoFactory = agentInfoFactory;
        }

        public Builder refreshInterval(long refreshIntervalMs) {
            this.refreshIntervalMs = refreshIntervalMs;
            return this;
        }

        public Builder sendInterval(long sendIntervalMs) {
            this.sendIntervalMs = sendIntervalMs;
            return this;
        }

        public Builder maxTryPerAttempt(int maxTryCountPerAttempt) {
            this.maxTryPerAttempt = maxTryCountPerAttempt;
            return this;
        }

        public AgentInfoSender build() {
            if (this.refreshIntervalMs <= 0) {
                throw new IllegalStateException("agentInfoRefreshIntervalMs must be greater than 0");
            }
            if (this.sendIntervalMs <= 0) {
                throw new IllegalStateException("agentInfoSendIntervalMs must be greater than 0");
            }
            if (this.maxTryPerAttempt <= 0) {
                throw new IllegalStateException("maxTryPerAttempt must be greater than 0");
            }
            return new AgentInfoSender(this);
        }
    }

}
