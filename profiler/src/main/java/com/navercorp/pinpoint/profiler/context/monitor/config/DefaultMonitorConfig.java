/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.monitor.config;

import com.navercorp.pinpoint.common.config.Value;

public class DefaultMonitorConfig implements MonitorConfig {

    public static final int DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS = 5 * 1000;
    public static final int DEFAULT_NUM_AGENT_STAT_BATCH_SEND = 6;

    @Value("${profiler.custommetric.enable}")
    private boolean customMetricEnable = false;
    @Value("${profiler.custommetric.limit.size}")
    private int customMetricLimitSize = 10;

    @Value("${profiler.uri.stat.enable}")
    private boolean uriStatEnable = false;
    @Value("${profiler.uri.stat.completed.data.limit.size}")
    private int completedUriStatDataLimitSize = 1000;


    @Value("${profiler.jvm.stat.collect.interval}")
    private int profileJvmStatCollectIntervalMs = DEFAULT_AGENT_STAT_COLLECTION_INTERVAL_MS;
    @Value("${profiler.jvm.stat.batch.send.count}")
    private int profileJvmStatBatchSendCount = DEFAULT_NUM_AGENT_STAT_BATCH_SEND;
    @Value("${profiler.jvm.stat.collect.detailed.metrics}")
    private boolean profilerJvmStatCollectDetailedMetrics = false;

    @Override
    public int getProfileJvmStatCollectIntervalMs() {
        return profileJvmStatCollectIntervalMs;
    }

    @Override
    public int getProfileJvmStatBatchSendCount() {
        return profileJvmStatBatchSendCount;
    }

    @Override
    public boolean isProfilerJvmStatCollectDetailedMetrics() {
        return profilerJvmStatCollectDetailedMetrics;
    }

    @Override
    public boolean isCustomMetricEnable() {
        return customMetricEnable;
    }

    @Override
    public int getCustomMetricLimitSize() {
        return customMetricLimitSize;
    }

    @Override
    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    @Override
    public int getCompletedUriStatDataLimitSize() {
        return completedUriStatDataLimitSize;
    }


    @Override
    public String toString() {
        return "DefaultMonitorConfig{" +
                "customMetricEnable=" + customMetricEnable +
                ", customMetricLimitSize=" + customMetricLimitSize +
                ", uriStatEnable=" + uriStatEnable +
                ", completedUriStatDataLimitSize=" + completedUriStatDataLimitSize +
                ", profileJvmStatCollectIntervalMs=" + profileJvmStatCollectIntervalMs +
                ", profileJvmStatBatchSendCount=" + profileJvmStatBatchSendCount +
                ", profilerJvmStatCollectDetailedMetrics=" + profilerJvmStatCollectDetailedMetrics +
                '}';
    }
}
