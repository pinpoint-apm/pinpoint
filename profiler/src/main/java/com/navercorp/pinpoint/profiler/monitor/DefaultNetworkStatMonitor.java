/*
 * Copyright 2023 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.monitor.config.DefaultMonitorConfig;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultNetworkStatMonitor implements NetworkStatMonitor {
    private static final long MIN_COLLECTION_INTERVAL_MS = 1000 * 5;
    private static final long MAX_COLLECTION_INTERVAL_MS = 1000 * 10;
    private static final long DEFAULT_COLLECTION_INTERVAL_MS = DefaultMonitorConfig.DEFAULT_NETWORK_METRIC_COLLECTION_INTERVAL_MS;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final long collectionIntervalMs;
    private final StatMonitorJob statMonitorJob;
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-hw-stat-monitor", true));
    private final boolean isNetworkMetricEnable;

    @Inject
    public DefaultNetworkStatMonitor(@StatDataSender DataSender<MetricType> dataSender,
                                     MonitorConfig monitorConfig) {
        long collectionIntervalMs = monitorConfig.getNetworkMetricCollectIntervalMs();

        if (collectionIntervalMs < MIN_COLLECTION_INTERVAL_MS) {
            collectionIntervalMs = DEFAULT_COLLECTION_INTERVAL_MS;
        }
        if (collectionIntervalMs > MAX_COLLECTION_INTERVAL_MS) {
            collectionIntervalMs = DEFAULT_COLLECTION_INTERVAL_MS;
        }

        this.collectionIntervalMs = collectionIntervalMs;
        List<Runnable> runnableList = new ArrayList<>();
        this.isNetworkMetricEnable = monitorConfig.isNetworkMetricEnable();

        if (isNetworkMetricEnable && NetworkMetricCollectingJob.isSupported()) {
            Runnable networkMetricCollectingJob = new NetworkMetricCollectingJob(dataSender,
                    monitorConfig.isUdpStatsEnable(), monitorConfig.isTcpStatsEnable(), collectionIntervalMs);
            runnableList.add(networkMetricCollectingJob);
        }
        this.statMonitorJob = new StatMonitorJob(runnableList);

    }
    @Override
    public void start() {
        if (isNetworkMetricEnable) {
            executor.scheduleAtFixedRate(statMonitorJob, this.collectionIntervalMs, this.collectionIntervalMs, TimeUnit.MILLISECONDS);
            logger.info("HW stat monitor started");
        } else {
            logger.info("HW stat monitor disabled");
        }
    }

    @Override
    public void stop() {
        if (isNetworkMetricEnable) {
            statMonitorJob.close();
            executor.shutdown();
            try {
                executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("HW stat monitor stopped");
        }
    }
}
