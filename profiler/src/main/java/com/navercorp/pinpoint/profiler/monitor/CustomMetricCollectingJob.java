/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentCustomMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class CustomMetricCollectingJob implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DataSender dataSender;
    private final AgentCustomMetricCollector agentCustomMetricCollector;
    private final int numCollectionsPerBatch;

    // Not thread safe. For use with single thread ONLY
    private int collectCount = 0;
    private long prevCollectionTimestamp = System.currentTimeMillis();
    private List<AgentCustomMetricSnapshot> agentCustomMetricSnapshotList;

    public CustomMetricCollectingJob(DataSender dataSender, AgentCustomMetricCollector agentCustomMetricCollector, int numCollectionsPerBatch) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
        this.agentCustomMetricCollector = Assert.requireNonNull(agentCustomMetricCollector, "agentCustomMetricCollector");
        Assert.isTrue(numCollectionsPerBatch > 0, "numCollectionsPerBatch must be `numCollectionsPerBatch > 0`");
        this.numCollectionsPerBatch = numCollectionsPerBatch;
        this.agentCustomMetricSnapshotList = new ArrayList<AgentCustomMetricSnapshot>(numCollectionsPerBatch);
    }

    @Override
    public void run() {
        final long currentCollectionTimestamp = System.currentTimeMillis();
        final long collectInterval = currentCollectionTimestamp - this.prevCollectionTimestamp;
        try {
            final AgentCustomMetricSnapshot agentCustomMetricSnapshot = agentCustomMetricCollector.collect();
            agentCustomMetricSnapshot.setTimestamp(currentCollectionTimestamp);
            agentCustomMetricSnapshot.setCollectInterval(collectInterval);
            this.agentCustomMetricSnapshotList.add(agentCustomMetricSnapshot);
            if (++this.collectCount >= numCollectionsPerBatch) {
                send();
                this.collectCount = 0;
            }
        } catch (Exception ex) {
            logger.warn("CustomMetric collect failed. Caused:{}", ex.getMessage(), ex);
        } finally {
            this.prevCollectionTimestamp = currentCollectionTimestamp;
        }
    }

    private void send() {
        final AgentCustomMetricSnapshotBatch agentCustomMetricSnapshotBatch = new AgentCustomMetricSnapshotBatch(agentCustomMetricSnapshotList);

        logger.trace("collect agentCustomMetric:{}", agentCustomMetricSnapshotBatch);
        dataSender.send(agentCustomMetricSnapshotBatch);

        this.agentCustomMetricSnapshotList = new ArrayList<AgentCustomMetricSnapshot>(numCollectionsPerBatch);
    }

}
