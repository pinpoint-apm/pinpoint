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

package com.navercorp.pinpoint.collector.heatmap.vo;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;

import java.util.Objects;

/**
 * Aggregation key for heatmap stats, shared by application and agent level tables.
 * eventTime is rounded to the time slot, elapsedTime to 200ms buckets.
 */
public record HeatmapStatKey(String serviceName,
                             String applicationName,
                             String agentId,
                             long eventTime,
                             int elapsedTime,
                             boolean success) {

    public static final int ELAPSED_TIME_INTERVAL = 200;

    public HeatmapStatKey {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");
    }

    public static HeatmapStatKey of(SpanBo spanBo, TimeSlot timeSlot) {
        return of(spanBo.getServiceName(), spanBo.getApplicationName(), spanBo.getAgentId(),
                timeSlot.getTimeSlot(spanBo.getCollectorAcceptTime()), spanBo.getElapsed(), spanBo.getErrCode());
    }

    public static HeatmapStatKey of(String serviceName, String applicationName, String agentId, long eventTime, int elapsedTime, int errCode) {
        return new HeatmapStatKey(serviceName, applicationName, agentId, eventTime, roundUpElapsedTime(elapsedTime), errCode == 0);
    }

    static int roundUpElapsedTime(int elapsedTime) {
        return (((elapsedTime - 1) / ELAPSED_TIME_INTERVAL) + 1) * ELAPSED_TIME_INTERVAL;
    }
}
