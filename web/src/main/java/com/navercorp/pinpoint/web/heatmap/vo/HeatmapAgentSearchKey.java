/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.heatmap.vo;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimePrecision;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Query parameter for the agent-level heatmap table. agentId is null for application-level aggregation.
 */
public class HeatmapAgentSearchKey {
    private static final int RESULT_COUNT = 2;

    private final String tableName;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;

    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    private final int maxYAsix;
    private final int minYAsix;
    private final int elapsedTimeInterval;
    private final int largestMultiple;

    public HeatmapAgentSearchKey(String tableName, String serviceName, String applicationName, String agentId,
                                 TimeWindow timeWindow, ElapsedTimeBucketInfo bucketInfo) {
        this.tableName = StringPrecondition.requireHasLength(tableName, "tableName");
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = agentId;

        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(bucketInfo, "bucketInfo");
        this.range = timeWindow.getWindowRange();
        this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, timeWindow.getWindowSlotSize());
        this.limit = (long) timeWindow.getWindowRangeCount() * bucketInfo.bucketList().size() * RESULT_COUNT;

        this.elapsedTimeInterval = bucketInfo.timeInterval();
        this.minYAsix = bucketInfo.min();
        this.maxYAsix = bucketInfo.max();
        this.largestMultiple = bucketInfo.findLargestMultipleBelow();
    }

    public String getTableName() {
        return tableName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public Range getRange() {
        return range;
    }

    public TimePrecision getTimePrecision() {
        return timePrecision;
    }

    public long getLimit() {
        return limit;
    }

    public int getMaxYAsix() {
        return maxYAsix;
    }

    public int getMinYAsix() {
        return minYAsix;
    }

    public int getElapsedTimeInterval() {
        return elapsedTimeInterval;
    }

    public int getLargestMultiple() {
        return largestMultiple;
    }

    @Override
    public String toString() {
        return "HeatmapAgentSearchKey{" +
                "tableName='" + tableName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", range=" + range +
                ", limit=" + limit +
                '}';
    }
}