/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectJob implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final DataSender<MetricType> dataSender;
    private final AgentId agentId;
    private final long agentStartTimestamp;
    private final AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatCollector;
    private final int numCollectionsPerBatch;

    // Not thread safe. For use with single thread ONLY
    private int collectCount = 0;
    private long prevCollectionTimestamp = System.currentTimeMillis();
    private List<AgentStatMetricSnapshot> agentStats;

    public CollectJob(DataSender<MetricType> dataSender,
                      AgentId agentId, long agentStartTimestamp,
                      AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatCollector,
                      int numCollectionsPerBatch) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.agentId = agentId;
        this.agentStartTimestamp = agentStartTimestamp;
        this.agentStatCollector = agentStatCollector;
        this.numCollectionsPerBatch = numCollectionsPerBatch;
        this.agentStats = new ArrayList<>(numCollectionsPerBatch);
    }

    @Override
    public void run() {
        final long currentCollectionTimestamp = System.currentTimeMillis();
        final long collectInterval = currentCollectionTimestamp - this.prevCollectionTimestamp;
        try {
            final AgentStatMetricSnapshot agentStat = agentStatCollector.collect();
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
        final AgentStatMetricSnapshotBatch agentStatBatch = new AgentStatMetricSnapshotBatch();
        agentStatBatch.setAgentId(AgentId.unwrap(agentId));
        agentStatBatch.setStartTimestamp(agentStartTimestamp);
        agentStatBatch.setAgentStats(this.agentStats);
        // If we reuse agentStats list, there could be concurrency issue because data sender runs in a different
        // thread.
        // So create new list.
        this.agentStats = new ArrayList<>(numCollectionsPerBatch);
        logger.trace("collect agentStat:{}", agentStatBatch);
        dataSender.send(agentStatBatch);
    }
}