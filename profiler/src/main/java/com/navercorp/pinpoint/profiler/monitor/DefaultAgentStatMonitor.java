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

package com.navercorp.pinpoint.profiler.monitor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EmptyDataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AgentStat monitor
 *
 * @author harebox
 * @author hyungil.jeong
 */
public class DefaultAgentStatMonitor implements AgentStatMonitor {

    private static final long MIN_COLLECTION_INTERVAL_MS = 1000;
    private static final long MAX_COLLECTION_INTERVAL_MS = 1000 * 5;
    private static final long DEFAULT_COLLECTION_INTERVAL_MS = DefaultProfilerConfig.DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS;
    private static final int DEFAULT_NUM_COLLECTIONS_PER_SEND = DefaultProfilerConfig.DEFAULT_NUM_AGENT_STAT_BATCH_SEND;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final long collectionIntervalMs;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

    private final CollectJob collectJob;

    @Inject
    public DefaultAgentStatMonitor(@StatDataSender DataSender dataSender,
                                   @AgentId String agentId, @AgentStartTime long agentStartTimestamp,
                                   @Named("AgentStatCollector") AgentStatMetricCollector<TAgentStat> agentStatCollector,
                                   ProfilerConfig profilerConfig) {
        this(dataSender, agentId, agentStartTimestamp, agentStatCollector, profilerConfig.getProfileJvmStatCollectIntervalMs(), profilerConfig.getProfileJvmStatBatchSendCount());
    }

    public DefaultAgentStatMonitor(DataSender dataSender,
                                   String agentId, long agentStartTimestamp,
                                   AgentStatMetricCollector<TAgentStat> agentStatCollector,
                                   long collectionIntervalMs, int numCollectionsPerBatch) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (agentStatCollector == null) {
            throw new NullPointerException("agentStatCollector must not be null");
        }
        if (collectionIntervalMs < MIN_COLLECTION_INTERVAL_MS) {
            collectionIntervalMs = DEFAULT_COLLECTION_INTERVAL_MS;
        }
        if (collectionIntervalMs > MAX_COLLECTION_INTERVAL_MS) {
            collectionIntervalMs = DEFAULT_COLLECTION_INTERVAL_MS;
        }
        if (numCollectionsPerBatch < 1) {
            numCollectionsPerBatch = DEFAULT_NUM_COLLECTIONS_PER_SEND;
        }
        this.collectionIntervalMs = collectionIntervalMs;
        this.collectJob = new CollectJob(dataSender, agentId, agentStartTimestamp, agentStatCollector, numCollectionsPerBatch);

        preLoadClass(agentId, agentStartTimestamp, agentStatCollector);
    }

    // https://github.com/naver/pinpoint/issues/2881
    // #2881 AppClassLoader and PinpointUrlClassLoader Circular dependency deadlock
    // prevent deadlock for JDK6
    // Single thread execution is more safe than multi thread execution.
    // eg) executor.scheduleAtFixedRate(collectJob, 0(initialDelay is zero), this.collectionIntervalMs, TimeUnit.MILLISECONDS);
    private void preLoadClass(String agentId, long agentStartTimestamp, AgentStatMetricCollector<TAgentStat> agentStatCollector) {
        logger.debug("pre-load class start");
        CollectJob collectJob = new CollectJob(EmptyDataSender.INSTANCE, agentId, agentStartTimestamp, agentStatCollector, 1);

        // It is called twice to initialize some fields.
        collectJob.run();
        collectJob.run();
        logger.debug("pre-load class end");
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(collectJob, this.collectionIntervalMs, this.collectionIntervalMs, TimeUnit.MILLISECONDS);
        logger.info("AgentStat monitor started");
    }

    @Override
    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("AgentStat monitor stopped");
    }

}
