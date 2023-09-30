/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ATCDemand implements RealtimeDemand {

    private long id;
    private String applicationName;
    private String agentId;
    private long startTimestamp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ATCDemand demand = (ATCDemand) o;
        return id == demand.id && startTimestamp == demand.startTimestamp && Objects.equals(applicationName, demand.applicationName) && Objects.equals(agentId, demand.agentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, applicationName, agentId, startTimestamp);
    }

    @Override
    public String toString() {
        return "ATCDemand{" +
                "id=" + id +
                ", clusterKey='" + applicationName + ':' + agentId + ':' + startTimestamp +
                '}';
    }
}
