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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PCustomMetric;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.grpc.trace.PDoubleValue;
import com.navercorp.pinpoint.grpc.trace.PDouleGaugeMetric;
import com.navercorp.pinpoint.grpc.trace.PIntCountMetric;
import com.navercorp.pinpoint.grpc.trace.PIntGaugeMetric;
import com.navercorp.pinpoint.grpc.trace.PIntValue;
import com.navercorp.pinpoint.grpc.trace.PLongCountMetric;
import com.navercorp.pinpoint.grpc.trace.PLongGaugeMetric;
import com.navercorp.pinpoint.grpc.trace.PLongValue;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.CustomMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.DoubleGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongGaugeMetricVo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class GrpcCustomMetricMessageConverter implements MessageConverter<PCustomMetricMessage> {

    @Override
    public PCustomMetricMessage toMessage(Object message) {
        Assert.requireNonNull(message, "message");

        if (message instanceof AgentCustomMetricSnapshotBatch) {
            AgentCustomMetricSnapshotBatch agentCustomMetricSnapshotBatch = (AgentCustomMetricSnapshotBatch) message;
            List<AgentCustomMetricSnapshot> agentCustomMetricSnapshotList = agentCustomMetricSnapshotBatch.getAgentCustomMetricSnapshotList();

            Set<String> metricNameSet = new HashSet<String>();
            for (AgentCustomMetricSnapshot agentCustomMetricSnapshot : agentCustomMetricSnapshotList) {
                metricNameSet.addAll(agentCustomMetricSnapshot.getMetricNameSet());
            }

            PCustomMetricMessage.Builder builder = PCustomMetricMessage.newBuilder();
            for (int i = 0; i < agentCustomMetricSnapshotList.size(); i++) {
                AgentCustomMetricSnapshot agentCustomMetricSnapshot = agentCustomMetricSnapshotList.get(i);

                builder.addTimestamp(agentCustomMetricSnapshot.getTimestamp());
                builder.addCollectInterval(agentCustomMetricSnapshot.getCollectInterval());
            }

            for (String metricName : metricNameSet) {
                PCustomMetric pCustomMetric = create(metricName, agentCustomMetricSnapshotList);
                if (pCustomMetric != null) {
                    builder.addCustomMetrics(pCustomMetric);
                }
            }

            return builder.build();
        } else {
            throw new IllegalArgumentException("Not supported Object. clazz:" + message.getClass());
        }
    }

    private PCustomMetric create(String metricName, List<AgentCustomMetricSnapshot> agentCustomMetricSnapshotList) {
        int size = agentCustomMetricSnapshotList.size();

        CustomMetricVo representativeCustomMetricVo = null;
        CustomMetricVo[] customMetricVos = new CustomMetricVo[size];
        for (int i = 0; i < size; i++) {
            AgentCustomMetricSnapshot agentCustomMetricSnapshot = agentCustomMetricSnapshotList.get(i);
            CustomMetricVo customMetricVo = agentCustomMetricSnapshot.get(metricName);
            customMetricVos[i] = customMetricVo;
            if (customMetricVo == null) {
                continue;
            }
            if (representativeCustomMetricVo == null) {
                representativeCustomMetricVo = customMetricVo;
            }
        }

        return create0(metricName, representativeCustomMetricVo, customMetricVos);
    }

    private PCustomMetric create0(String metricName, CustomMetricVo representativeCustomMetricVo, CustomMetricVo[] customMetricVos) {
        if (representativeCustomMetricVo instanceof IntCountMetricVo) {
            return createIntCountMetric(metricName, customMetricVos);
        }
        if (representativeCustomMetricVo instanceof LongCountMetricVo) {
            return createLongCountMetric(metricName, customMetricVos);
        }
        if (representativeCustomMetricVo instanceof IntGaugeMetricVo) {
            return createIntGaugeMetric(metricName, customMetricVos);
        }
        if (representativeCustomMetricVo instanceof LongGaugeMetricVo) {
            return createLongGaugeMetric(metricName, customMetricVos);
        }
        if (representativeCustomMetricVo instanceof DoubleGaugeMetricVo) {
            return createDoubleGaugeMetric(metricName, customMetricVos);
        }
        return null;
    }

    // raw data              : 10, 12, 13, 14, 14
    // count metric format   : 10, 2, 1, 1, 0
    private PCustomMetric createIntCountMetric(String metricName, CustomMetricVo[] customMetricVos) {
        PIntCountMetric.Builder intCountMetricBuilder = PIntCountMetric.newBuilder();
        intCountMetricBuilder.setName(metricName);

        int prevValue = 0;
        for (CustomMetricVo customMetricVo : customMetricVos) {
            if (customMetricVo instanceof IntCountMetricVo) {
                int value = ((IntCountMetricVo) customMetricVo).getValue();
                intCountMetricBuilder.addValues(createIntValue(value - prevValue));
                prevValue = value;
            } else {
                intCountMetricBuilder.addValues(createNotSetIntValue());
            }
        }

        PCustomMetric.Builder customMetricBuilder = PCustomMetric.newBuilder();
        customMetricBuilder.setIntCountMetric(intCountMetricBuilder.build());

        return customMetricBuilder.build();
    }

    // raw data              : 10, 12, 13, 14, 14
    // count metric format   : 10, 2, 1, 1, 0
    private PCustomMetric createLongCountMetric(String metricName, CustomMetricVo[] customMetricVos) {
        PLongCountMetric.Builder longCountMetricBuilder = PLongCountMetric.newBuilder();
        longCountMetricBuilder.setName(metricName);

        long prevValue = 0;
        for (CustomMetricVo customMetricVo : customMetricVos) {
            if (customMetricVo instanceof LongCountMetricVo) {
                long value = ((LongCountMetricVo) customMetricVo).getValue();
                longCountMetricBuilder.addValues(createLongValue(value - prevValue));
                prevValue = value;
            } else {
                longCountMetricBuilder.addValues(createNotSetLongValue());
            }
        }

        PCustomMetric.Builder customMetricBuilder = PCustomMetric.newBuilder();
        customMetricBuilder.setLongCountMetric(longCountMetricBuilder.build());

        return customMetricBuilder.build();
    }

    private PCustomMetric createIntGaugeMetric(String metricName, CustomMetricVo[] customMetricVos) {
        PIntGaugeMetric.Builder intGaugeMetricBuilder = PIntGaugeMetric.newBuilder();
        intGaugeMetricBuilder.setName(metricName);

        for (CustomMetricVo customMetricVo : customMetricVos) {
            if (customMetricVo instanceof IntGaugeMetricVo) {
                int value = ((IntGaugeMetricVo) customMetricVo).getValue();
                intGaugeMetricBuilder.addValues(createIntValue(value));
            } else {
                intGaugeMetricBuilder.addValues(createNotSetIntValue());
            }
        }

        PCustomMetric.Builder customMetricBuilder = PCustomMetric.newBuilder();
        customMetricBuilder.setIntGaugeMetric(intGaugeMetricBuilder.build());

        return customMetricBuilder.build();
    }

    private PCustomMetric createLongGaugeMetric(String metricName, CustomMetricVo[] customMetricVos) {
        PLongGaugeMetric.Builder longGaugeMetricBuilder = PLongGaugeMetric.newBuilder();
        longGaugeMetricBuilder.setName(metricName);

        for (CustomMetricVo customMetricVo : customMetricVos) {
            if (customMetricVo instanceof LongGaugeMetricVo) {
                long value = ((LongGaugeMetricVo) customMetricVo).getValue();
                longGaugeMetricBuilder.addValues(createLongValue(value));
            } else {
                longGaugeMetricBuilder.addValues(createNotSetLongValue());
            }
        }

        PCustomMetric.Builder customMetricBuilder = PCustomMetric.newBuilder();
        customMetricBuilder.setLongGaugeMetric(longGaugeMetricBuilder.build());

        return customMetricBuilder.build();
    }

    private PCustomMetric createDoubleGaugeMetric(String metricName, CustomMetricVo[] customMetricVos) {
        PDouleGaugeMetric.Builder doubleGaugeMetricBuilder = PDouleGaugeMetric.newBuilder();
        doubleGaugeMetricBuilder.setName(metricName);

        for (CustomMetricVo customMetricVo : customMetricVos) {
            if (customMetricVo instanceof DoubleGaugeMetricVo) {
                double value = ((DoubleGaugeMetricVo) customMetricVo).getValue();
                doubleGaugeMetricBuilder.addValues(createDoubleValue(value));
            } else {
                doubleGaugeMetricBuilder.addValues(createNotSetDoubleValue());
            }
        }

        PCustomMetric.Builder customMetricBuilder = PCustomMetric.newBuilder();
        customMetricBuilder.setDoubleGaugeMetric(doubleGaugeMetricBuilder.build());

        return customMetricBuilder.build();
    }

    private PIntValue createIntValue(int value) {
        PIntValue.Builder builder = PIntValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    private PIntValue createNotSetIntValue() {
        PIntValue.Builder builder = PIntValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }

    private PLongValue createLongValue(long value) {
        PLongValue.Builder builder = PLongValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    private PLongValue createNotSetLongValue() {
        PLongValue.Builder builder = PLongValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }

    private PDoubleValue createDoubleValue(double value) {
        PDoubleValue.Builder builder = PDoubleValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    private PDoubleValue createNotSetDoubleValue() {
        PDoubleValue.Builder builder = PDoubleValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }

}