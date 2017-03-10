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
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

    private static final long DEFAULT_COLLECTION_INTERVAL_MS = 1000 * 5;
    private static final int DEFAULT_NUM_COLLECTIONS_PER_SEND = 6;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final long collectionIntervalMs;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

    private final CollectJob collectJob;

    @Inject
    public DefaultAgentStatMonitor(@StatDataSender DataSender dataSender,
                                   @AgentId String agentId, @AgentStartTime long agentStartTimestamp,
                                   @Named("AgentStatCollector") AgentStatMetricCollector<TAgentStat> agentStatCollector) {
        this(dataSender, agentId, agentStartTimestamp, agentStatCollector, DEFAULT_COLLECTION_INTERVAL_MS, DEFAULT_NUM_COLLECTIONS_PER_SEND);
    }

    public DefaultAgentStatMonitor(DataSender dataSender,
                                   String agentId, long agentStartTimestamp,
                                   AgentStatMetricCollector<TAgentStat> agentStatCollector,
                                   long collectionInterval, int numCollectionsPerBatch) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (agentStatCollector == null) {
            throw new NullPointerException("agentStatCollector must not be null");
        }
        this.collectionIntervalMs = collectionInterval;
        this.collectJob = new CollectJob(dataSender, agentId, agentStartTimestamp, agentStatCollector, numCollectionsPerBatch);
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

    // NotThreadSafe
    private static class CollectJob implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final DataSender dataSender;
        private final String agentId;
        private final long agentStartTimestamp;
        private final AgentStatMetricCollector<TAgentStat> agentStatCollector;
        private final int numCollectionsPerBatch;

        // Not thread safe. For use with single thread ONLY
        private int collectCount = 0;
        private long prevCollectionTimestamp = System.currentTimeMillis();
        private List<TAgentStat> agentStats;

        private CollectJob(DataSender dataSender,
                           String agentId, long agentStartTimestamp,
                           AgentStatMetricCollector<TAgentStat> agentStatCollector,
                           int numCollectionsPerBatch) {
            this.dataSender = dataSender;
            this.agentId = agentId;
            this.agentStartTimestamp = agentStartTimestamp;
            this.agentStatCollector = agentStatCollector;
            this.numCollectionsPerBatch = numCollectionsPerBatch;
            this.agentStats = new ArrayList<TAgentStat>(numCollectionsPerBatch);
        }

        @Override
        public void run() {
            final long currentCollectionTimestamp = System.currentTimeMillis();
            final long collectInterval = currentCollectionTimestamp - this.prevCollectionTimestamp;
            try {
                final TAgentStat agentStat = agentStatCollector.collect();
                agentStat.setTimestamp(currentCollectionTimestamp);
                agentStat.setCollectInterval(collectInterval);
                this.agentStats.add(agentStat);
                if (++this.collectCount >= numCollectionsPerBatch) {
                    sendAgentStats();
                    this.collectCount = 0;
                }
            } catch (Exception ex) {
                logger.warn("AgentStat collect failed. Caused:{}", ex.getMessage(), ex);
            } finally {
                this.prevCollectionTimestamp = currentCollectionTimestamp;
            }
        }

        private void sendAgentStats() {
            // prepare TAgentStat object.
            // TODO multi thread issue.
            // If we reuse TAgentStat, there could be concurrency issue because data sender runs in a different thread.
            final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
            agentStatBatch.setAgentId(agentId);
            agentStatBatch.setStartTimestamp(agentStartTimestamp);
            agentStatBatch.setAgentStats(this.agentStats);
            // If we reuse agentStats list, there could be concurrency issue because data sender runs in a different
            // thread.
            // So create new list.
            this.agentStats = new ArrayList<TAgentStat>(numCollectionsPerBatch);
            logger.trace("collect agentStat:{}", agentStatBatch);
            dataSender.send(agentStatBatch);
        }
    }

}
