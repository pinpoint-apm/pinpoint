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

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.profiler.monitor.metric.custom.CustomMetricVo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class AgentCustomMetricSnapshot {

    private long timestamp;
    private long collectInterval;

    private final Map<String, CustomMetricVo> customMetricVoMap;

    public AgentCustomMetricSnapshot(int size) {
        this.customMetricVoMap = new HashMap<String, CustomMetricVo>(size);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

    public boolean add(CustomMetricVo customMetricVo) {
        CustomMetricVo put = customMetricVoMap.put(customMetricVo.getName(), customMetricVo);
        return put == null;
    }

    public Set<String> getMetricNameSet() {
        return customMetricVoMap.keySet();
    }

    public CustomMetricVo get(String metricName) {
        return customMetricVoMap.get(metricName);
    }

    public Map<String, CustomMetricVo> getCustomMetricVoMap() {
        return customMetricVoMap;
    }

    @Override
    public String toString() {
        return "AgentCustomMetricSnapshot{" +
            "timestamp=" + timestamp +
            ", collectInterval=" + collectInterval +
            ", customMetricVoMap=" + customMetricVoMap +
            '}';
    }

}
