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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentCustomMetricBo {

    private String agentId;

    private long startTimestamp;

    private final Map<String, IntCounterMetricValueList> intCounterMetricValueListMap = new HashMap<>();

    private final Map<String, LongCounterMetricValueList> longCounterMetricValueListMap = new HashMap<>();

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

    public boolean addIntCounterMetricValueList(IntCounterMetricValueList intCounterMetricValueList) {
        String metricName = intCounterMetricValueList.getMetricName();
        if (StringUtils.isEmpty(metricName)) {
            return false;
        }

        IntCounterMetricValueList oldValue = intCounterMetricValueListMap.putIfAbsent(metricName, intCounterMetricValueList);
        return oldValue == null;
    }

    public boolean addLongCounterMetricValueList(LongCounterMetricValueList longCounterMetricValueList) {
        String metricName = longCounterMetricValueList.getMetricName();
        if (StringUtils.isEmpty(metricName)) {
            return false;
        }

        LongCounterMetricValueList oldValue = longCounterMetricValueListMap.putIfAbsent(metricName, longCounterMetricValueList);
        return oldValue == null;
    }

    public IntCounterMetricValueList getIntCounterMetricValueList(String metricName) {
        return intCounterMetricValueListMap.get(metricName);
    }

    public LongCounterMetricValueList getLongCounterMetricValueList(String metricName) {
        return longCounterMetricValueListMap.get(metricName);
    }

    @Override
    public String toString() {
        return "AgentCustomMetricBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", intCounterMetricValueListMap=" + intCounterMetricValueListMap +
                ", longCounterMetricValueListMap=" + longCounterMetricValueListMap +
                '}';
    }

}
