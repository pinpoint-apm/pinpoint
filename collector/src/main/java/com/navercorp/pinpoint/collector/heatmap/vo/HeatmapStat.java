/*
 * Copyright 2025 NAVER Corp.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.collector.heatmap.util.HashmapSortKeyUtils;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatmapStat {

    private final String applicationName;
    private final String agentId;
    private final String sortKey;
    private final long eventTime;
    private final int elapsedTime;
    private final Boolean isSuccess;

    public HeatmapStat(String applicationName, String agentId, long eventTime, int elapsedTime, int errCode) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.eventTime = eventTime;
        this.elapsedTime = (((elapsedTime - 1) / 200) + 1) * 200;
        this.isSuccess = errCode == 0;
        this.sortKey = HashmapSortKeyUtils.generateKey(applicationName, isSuccess);

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

    public String getSortKey() {
        return sortKey;
    }

    @JsonIgnore
    public Boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String toString() {
        return "HeatmapStat{" +
                "applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", eventTime=" + eventTime +
                ", elapsedTime=" + elapsedTime +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
