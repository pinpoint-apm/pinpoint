/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatInfo;
import com.navercorp.pinpoint.profiler.sender.AsyncQueueingExecutor;
import com.navercorp.pinpoint.profiler.sender.AsyncQueueingExecutorListener;

import io.netty.util.internal.shaded.org.jctools.queues.atomic.SpscLinkedAtomicQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Queue;

/**
 * @author Taejin Koo
 */
public class AsyncQueueingUriStatStorage extends AsyncQueueingExecutor<UriStatInfo> implements UriStatStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueingUriStatStorage.class);

    private final ExecutorListener executorListener;

    public AsyncQueueingUriStatStorage(int queueSize, int collectInterval, String executorName) {
        this(queueSize, executorName, new ExecutorListener(collectInterval));
    }

    private AsyncQueueingUriStatStorage(int queueSize, String executorName, ExecutorListener executorListener) {
        super(queueSize, executorName, executorListener);
        this.executorListener = executorListener;
    }

    @Override
    public void store(String uri, boolean status, long elapsedTime) {
        Assert.requireNonNull(uri, "uri");
        UriStatInfo uriStatInfo = new UriStatInfo(uri, status, elapsedTime);
        execute(uriStatInfo);
    }

    @Override
    public AgentUriStatData poll() {
        Queue<AgentUriStatData> completedDataQueue = executorListener.getCompletedDataQueue();
        return completedDataQueue.poll();
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    protected void pollTimeout(long timeout) {
        executorListener.executePollTimeout();
    }

    private static class ExecutorListener implements AsyncQueueingExecutorListener<UriStatInfo> {

        private final SpscLinkedAtomicQueue<AgentUriStatData> completedAgentUriStatDataQueue = new SpscLinkedAtomicQueue<>();

        private AgentUriStatData agentUriStatData;
        private int collectInterval;

        public ExecutorListener(int collectInterval) {
            Assert.isTrue(collectInterval > 0, "collectInterval must be ' > 0'");
            this.collectInterval = collectInterval;
        }

        @Override
        public void execute(Collection<UriStatInfo> messageList) {
            final long currentBaseTimestamp = getBaseTimestamp();
            checkAndFlushOldData(currentBaseTimestamp);

            AgentUriStatData agentUriStatData = getAgentUriStatData(currentBaseTimestamp);

            Object[] dataList = messageList.toArray();
            for (int i = 0; i < CollectionUtils.nullSafeSize(messageList); i++) {
                try {
                    agentUriStatData.add((UriStatInfo) dataList[i]);
                } catch (Throwable th) {
                    LOGGER.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
                }
            }
        }

        @Override
        public void execute(UriStatInfo message) {
            long currentBaseTimestamp = getBaseTimestamp();
            checkAndFlushOldData(currentBaseTimestamp);

            AgentUriStatData agentUriStatData = getAgentUriStatData(currentBaseTimestamp);
            agentUriStatData.add(message);
        }

        public void executePollTimeout() {
            long currentBaseTimestamp = getBaseTimestamp();
            checkAndFlushOldData(currentBaseTimestamp);
        }

        private long getBaseTimestamp() {
            long currentTimeMillis = System.currentTimeMillis();
            long timestamp = currentTimeMillis - (currentTimeMillis % collectInterval);
            return timestamp;
        }

        private boolean checkAndFlushOldData(long currentBaseTimestamp) {
            if (agentUriStatData == null) {
                return false;
            }

            if (currentBaseTimestamp > agentUriStatData.getBaseTimestamp()) {
                if (completedAgentUriStatDataQueue.size() > 10) {
                    completedAgentUriStatDataQueue.remove();
                }

                completedAgentUriStatDataQueue.offer(agentUriStatData);
                // TODO FLUSH
                agentUriStatData = null;
                return true;
            }
            return false;
        }

        private AgentUriStatData getAgentUriStatData(long currentBaseTimestamp) {
            if (agentUriStatData == null) {
                agentUriStatData = new AgentUriStatData(currentBaseTimestamp, collectInterval);
            }
            return agentUriStatData;
        }

        public Queue<AgentUriStatData> getCompletedDataQueue() {
            return completedAgentUriStatDataQueue;
        }
    }

}