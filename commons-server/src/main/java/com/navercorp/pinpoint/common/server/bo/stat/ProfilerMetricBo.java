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
package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfilerMetricBo implements AgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;

    private String agentId;
    private long startTimestamp;
    private long timestamp;
    private String metricName; // is this necessary?
    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, Double> values = new HashMap<>();

    @Override
    public String getAgentId() {
        return agentId;
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

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.PROFILER_METRIC;
    }

    public void addTags(String name, String value) {
        tags.put(name, value);
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void addValues(String name, double value) {
        values.put(name, value);
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public void setMetricName(String metricName) {
        this.metricName = Objects.requireNonNull(metricName);
    }

    public String getMetricName() {
        return metricName;
    }
}