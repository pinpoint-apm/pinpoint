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

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class EachCustomMetricBo implements AgentStatDataPoint {

    private final AgentStatType agentStatType;

    private String agentId;
    private long startTimestamp;
    private long timestamp;

    private final Map<String, CustomMetricValue> customMetricValueMap = new HashMap<>();

    public EachCustomMetricBo(AgentStatType agentStatType) {
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public AgentStatType getAgentStatType() {
        return agentStatType;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean put(String key, CustomMetricValue value) {
        final CustomMetricValue oldValue = customMetricValueMap.putIfAbsent(key, value);
        return oldValue == null;
    }

    public CustomMetricValue get(String key) {
        return customMetricValueMap.get(key);
    }

    public Set<String> keySet() {
        return customMetricValueMap.keySet();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EachCustomMetricBo{");
        sb.append("agentStatType=").append(agentStatType);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", customMetricValueMap=").append(customMetricValueMap);
        sb.append('}');
        return sb.toString();
    }

}


