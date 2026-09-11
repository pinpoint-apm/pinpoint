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

import java.util.Objects;

/**
 * Agent level heatmap record. seriesKey is derived by pinot ingestion transform.
 */
public class HeatmapAgentStat {
    private static final int ELAPSED_TIME_INTERVAL = 200;

    public static final String RESULT_TYPE_SUCCESS = "SUCCESS";
    public static final String RESULT_TYPE_FAILURE = "FAILURE";

    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final long eventTime;
    private final int elapsedTime;
    private final String resultType;

    public HeatmapAgentStat(String serviceName, String applicationName, String agentId, long eventTime, int elapsedTime, int errCode) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.eventTime = eventTime;
        this.elapsedTime = roundUpElapsedTime(elapsedTime);
        this.resultType = errCode == 0 ? RESULT_TYPE_SUCCESS : RESULT_TYPE_FAILURE;
    }

    static int roundUpElapsedTime(int elapsedTime) {
        return (((elapsedTime - 1) / ELAPSED_TIME_INTERVAL) + 1) * ELAPSED_TIME_INTERVAL;
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

    public long getEventTime() {
        return eventTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public String getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return "HeatmapAgentStat{" +
                "serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", eventTime=" + eventTime +
                ", elapsedTime=" + elapsedTime +
                ", resultType='" + resultType + '\'' +
                '}';
    }
}