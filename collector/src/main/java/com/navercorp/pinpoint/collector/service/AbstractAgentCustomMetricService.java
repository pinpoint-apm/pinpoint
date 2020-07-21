/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetric;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.common.server.bo.metric.AgentCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricType;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricValueList;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptor;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptors;
import com.navercorp.pinpoint.rpc.util.ListUtils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public abstract class AbstractAgentCustomMetricService implements AgentCustomMetricService {

    protected final CustomMetricType customMetricType;

    public AbstractAgentCustomMetricService(CustomMetricType customMetricType) {
        this.customMetricType = Objects.requireNonNull(customMetricType, "customMetricType");
    }

    protected CustomMetricType getCustomMetricType() {
        return customMetricType;
    }

    @Override
    public boolean isSupport(AgentCustomMetricBo agentCustomMetricBo) {
        Objects.requireNonNull(agentCustomMetricBo, "agentCustomMetricBo");

        final FieldDescriptors fieldDescriptors = customMetricType.getFieldDescriptors();
        final List<FieldDescriptor> fieldDescriptorList = fieldDescriptors.getAll();
        for (FieldDescriptor fieldDescriptor : fieldDescriptorList) {
            final CustomMetricValueList customMetricValueList = getCustomMetricValueList(agentCustomMetricBo, fieldDescriptor);
            if (customMetricValueList == null) {
                return false;
            }
        }

        return true;
    }

    private CustomMetricValueList getCustomMetricValueList(AgentCustomMetricBo agentCustomMetricBo, FieldDescriptor fieldDescriptor) {
        final String metricName = fieldDescriptor.getName();

        final Class<? extends CustomMetric> type = fieldDescriptor.getType();
        if (type == IntCounter.class) {
            return agentCustomMetricBo.getIntCounterMetricValueList(metricName);
        } else if (type == LongCounter.class) {
            return agentCustomMetricBo.getLongCounterMetricValueList(metricName);
        }

        return null;
    }

    @Override
    public List<EachCustomMetricBo> map(AgentCustomMetricBo agentCustomMetricBo) {
        MultiValueMap<Long, CustomMetricValue> multiValueMap = new LinkedMultiValueMap();

        final FieldDescriptors fieldDescriptors = customMetricType.getFieldDescriptors();
        final List<FieldDescriptor> fieldDescriptorList = fieldDescriptors.getAll();
        for (FieldDescriptor fieldDescriptor : fieldDescriptorList) {
            final CustomMetricValueList customMetricValueList = getCustomMetricValueList(agentCustomMetricBo, fieldDescriptor);
            addMetricBo(multiValueMap, customMetricValueList);
        }

        final String agentId = agentCustomMetricBo.getAgentId();
        final long startTimestamp = agentCustomMetricBo.getStartTimestamp();

        List<EachCustomMetricBo> result = create(agentId, startTimestamp, multiValueMap);
        return result;
    }


    private void addMetricBo(MultiValueMap<Long, CustomMetricValue> multiValueMap, CustomMetricValueList customMetricValueList) {
        final List<CustomMetricValue> valueList = customMetricValueList.getValueList();
        for (CustomMetricValue value : valueList) {
            multiValueMap.add(value.getTimestamp(), value);
        }
    }

    private List<EachCustomMetricBo> create(String agentId, long startTimestamp, MultiValueMap<Long, CustomMetricValue> multiValueMap) {
        List<EachCustomMetricBo> result = new ArrayList<>(multiValueMap.size());

        for (List<CustomMetricValue> values : multiValueMap.values()) {
            EachCustomMetricBo agentCustomMetricBo = new EachCustomMetricBo(customMetricType.getAgentStatType());

            CustomMetricValue first = ListUtils.getFirst(values);
            if (first == null) {
                continue;
            }

            agentCustomMetricBo.setAgentId(agentId);
            agentCustomMetricBo.setStartTimestamp(startTimestamp);
            agentCustomMetricBo.setTimestamp(first.getTimestamp());

            for (CustomMetricValue value : values) {
                String metricName = value.getMetricName();
                agentCustomMetricBo.put(metricName, value);
            }

            result.add(agentCustomMetricBo);
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("customMetricType=").append(customMetricType);
        sb.append('}');
        return sb.toString();
    }
}
