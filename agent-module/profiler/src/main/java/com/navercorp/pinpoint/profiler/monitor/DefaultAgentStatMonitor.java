/*
 * Copyright 2019 NAVER Corp.
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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.EmptyDataSender;
import com.navercorp.pinpoint.profiler.context.module.AgentIdHolder;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.monitor.config.DefaultMonitorConfig;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentCustomMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private static final long DEFAULT_COLLECTION_INTERVAL_MS = DefaultMonitorConfig.DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS;
    private static final int DEFAULT_NUM_COLLECTIONS_PER_SEND = DefaultMonitorConfig.DEFAULT_NUM_AGENT_STAT_BATCH_SEND;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final long collectionIntervalMs;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

    private final StatMonitorJob statMonitorJob;

    @Inject
    public DefaultAgentStatMonitor(@StatDataSender DataSender<MetricType> dataSender,
                                   @AgentIdHolder AgentId agentId,
                                   @AgentStartTime long agentStartTimestamp,
                                   @Named("AgentStatCollector") AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatCollector,
                                   CustomMetricRegistryService customMetricRegistryService,
                                   UriStatStorage uriStatStorage,
                                   MonitorConfig monitorConfig) {
        Objects.requireNonNull(dataSender, "dataSender");
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(agentStatCollector, "agentStatCollector");


        long collectionIntervalMs = monitorConfig.getProfileJvmStatCollectIntervalMs();
        int numCollectionsPerBatch = monitorConfig.getProfileJvmStatBatchSendCount();

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

        List<Runnable> runnableList = new ArrayList<>();

        Runnable statCollectingJob = new CollectJob(dataSender, agentId, agentStartTimestamp, agentStatCollector, numCollectionsPerBatch);
        runnableList.add(statCollectingJob);

        if (monitorConfig.isCustomMetricEnable() && customMetricRegistryService != null) {
            Runnable customMetricCollectionJob = new CustomMetricCollectingJob(dataSender, new AgentCustomMetricCollector(customMetricRegistryService), numCollectionsPerBatch);
            runnableList.add(customMetricCollectionJob);
        }

        if (monitorConfig.isUriStatEnable() && uriStatStorage != null) {
            Runnable uriStatCollectingJob = new UriStatCollectingJob(dataSender, uriStatStorage);
            runnableList.add(uriStatCollectingJob);
        }

        this.statMonitorJob = new StatMonitorJob(runnableList);

        preLoadClass(agentId, agentStartTimestamp, agentStatCollector);
    }

    // https://github.com/naver/pinpoint/issues/2881
    // #2881 AppClassLoader and PinpointUrlClassLoader Circular dependency deadlock
    // prevent deadlock for JDK6
    // Single thread execution is more safe than multi thread execution.
    // eg) executor.scheduleAtFixedRate(collectJob, 0(initialDelay is zero), this.collectionIntervalMs, TimeUnit.MILLISECONDS);
    private void preLoadClass(AgentId agentId, long agentStartTimestamp, AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatCollector) {
        logger.debug("pre-load class start");
        CollectJob collectJob = new CollectJob(EmptyDataSender.instance(), agentId, agentStartTimestamp, agentStatCollector, 1);

        // It is called twice to initialize some fields.
        collectJob.run();
        collectJob.run();
        logger.debug("pre-load class end");
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(statMonitorJob, this.collectionIntervalMs, this.collectionIntervalMs, TimeUnit.MILLISECONDS);
        logger.info("AgentStat monitor started");
    }

    @Override
    public void stop() {

        statMonitorJob.close();

        executor.shutdown();
        try {
            if (!executor.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                logger.warn("AgentStat monitor shutdown forcefully");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("AgentStat monitor stopped");
    }

}
