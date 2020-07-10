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

package com.navercorp.pinpoint.collector.mapper.grpc.metric;

import com.navercorp.pinpoint.common.server.bo.metric.AgentCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValueList;
import com.navercorp.pinpoint.common.server.bo.metric.LongCounterMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.LongCounterMetricValueList;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PCustomMetric;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.grpc.trace.PIntCountMetric;
import com.navercorp.pinpoint.grpc.trace.PIntValue;
import com.navercorp.pinpoint.grpc.trace.PLongCountMetric;
import com.navercorp.pinpoint.grpc.trace.PLongValue;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcAgentCustomMetricMapper {

    private final GrpcIntCountMetricMapper intCountMetricMapper = new GrpcIntCountMetricMapper();
    private final GrpcLongCountMetricMapper longCountMetricMapper = new GrpcLongCountMetricMapper();

    public AgentCustomMetricBo map(final PCustomMetricMessage customMetricMessage, final Header header) {
        if (customMetricMessage == null) {
            return null;
        }
        final String agentId = header.getAgentId();
        final long startTimestamp = header.getAgentStartTime();

        List<Long> timestampList = customMetricMessage.getTimestampList();
        List<Long> collectIntervalList = customMetricMessage.getCollectIntervalList();

        if (timestampList.size() != collectIntervalList.size()) {
            return null;
        }

        AgentCustomMetricBo agentCustomMetricBo = new AgentCustomMetricBo();
        agentCustomMetricBo.setAgentId(agentId);
        agentCustomMetricBo.setStartTimestamp(startTimestamp);

        List<PCustomMetric> customMetricsList = customMetricMessage.getCustomMetricsList();
        for (PCustomMetric pCustomMetric : customMetricsList) {
            if (pCustomMetric.hasIntCountMetric()) {
                IntCounterMetricValueList intCounterMetricValueList = createIntCountMetric(pCustomMetric, timestampList);
                if (intCounterMetricValueList != null) {
                    agentCustomMetricBo.addIntCounterMetricValueList(intCounterMetricValueList);
                }
            } else if (pCustomMetric.hasLongCountMetric()) {
                LongCounterMetricValueList longCounterMetricValueList = createLongCountMetric(pCustomMetric, timestampList);
                if (longCounterMetricValueList != null) {
                    agentCustomMetricBo.addLongCounterMetricValueList(longCounterMetricValueList);
                }
            } else {
                continue;
            }
        }

        return agentCustomMetricBo;
    }

    private IntCounterMetricValueList createIntCountMetric(PCustomMetric pCustomMetric, List<Long> timestampList) {
        PIntCountMetric intCountMetric = pCustomMetric.getIntCountMetric();

        List<PIntValue> intValuesList = intCountMetric.getValuesList();
        int valueSize = intValuesList.size();
        if (valueSize != timestampList.size()) {
            return null;
        }

        final String metricName = intCountMetric.getName();

        final IntCounterMetricValueList intCounterMetricValueList = new IntCounterMetricValueList(metricName);
        IntCounterMetricValue prevValue = null;
        for (int i = 0; i < valueSize; i++) {
            PIntValue pIntValue = intValuesList.get(i);

            IntCounterMetricValue intCounterMetricValue = intCountMetricMapper.map(pIntValue, prevValue);
            if (intCounterMetricValue != null) {
                intCounterMetricValue.setMetricName(metricName);

                Long timestamp = timestampList.get(i);
                intCounterMetricValue.setTimestamp(timestamp);

                intCounterMetricValueList.add(intCounterMetricValue);
            }
            prevValue = intCounterMetricValue;
        }

        return intCounterMetricValueList;
    }

    private LongCounterMetricValueList createLongCountMetric(PCustomMetric pCustomMetric, List<Long> timestampList) {
        PLongCountMetric longCountMetric = pCustomMetric.getLongCountMetric();

        List<PLongValue> longValuesList = longCountMetric.getValuesList();
        int valueSize = longValuesList.size();
        if (valueSize != timestampList.size()) {
            return null;
        }

        final String metricName = longCountMetric.getName();

        final LongCounterMetricValueList longCounterMetricValueList = new LongCounterMetricValueList(metricName);
        LongCounterMetricValue prevValue = null;
        for (int i = 0; i < valueSize; i++) {
            PLongValue pLongValue = longValuesList.get(i);

            LongCounterMetricValue longCounterMetricValue = longCountMetricMapper.map(pLongValue, prevValue);
            if (longCounterMetricValue != null) {
                longCounterMetricValue.setMetricName(metricName);

                Long timestamp = timestampList.get(i);
                longCounterMetricValue.setTimestamp(timestamp);

                longCounterMetricValueList.add(longCounterMetricValue);
            }
            prevValue = longCounterMetricValue;
        }

        return longCounterMetricValueList;
    }

}
