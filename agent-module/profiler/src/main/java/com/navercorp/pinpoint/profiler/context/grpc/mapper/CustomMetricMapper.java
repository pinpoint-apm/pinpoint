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
package com.navercorp.pinpoint.profiler.context.grpc.mapper;

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
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.CustomMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.DoubleGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.IntGaugeMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongCountMetricVo;
import com.navercorp.pinpoint.profiler.monitor.metric.custom.LongGaugeMetricVo;
import org.mapstruct.AfterMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author intr3p1d
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
        }
)
public interface CustomMetricMapper {

    default PCustomMetricMessage map(AgentCustomMetricSnapshotBatch message) {
        List<AgentCustomMetricSnapshot> agentCustomMetricSnapshotList = message.getAgentCustomMetricSnapshotList();

        Set<String> metricNameSet = new HashSet<>();
        for (AgentCustomMetricSnapshot agentCustomMetricSnapshot : agentCustomMetricSnapshotList) {
            metricNameSet.addAll(agentCustomMetricSnapshot.getMetricNameSet());
        }

        PCustomMetricMessage.Builder builder = PCustomMetricMessage.newBuilder();
        for (AgentCustomMetricSnapshot agentCustomMetricSnapshot : agentCustomMetricSnapshotList) {
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
    }

    default PCustomMetric create(
            String metricName,
            List<AgentCustomMetricSnapshot> agentCustomMetricSnapshotList
    ) {
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

        return map(metricName, representativeCustomMetricVo, customMetricVos);
    }


    default PCustomMetric map(
            String metricName,
            CustomMetricVo representativeCustomMetricVo,
            CustomMetricVo[] customMetricVos
    ) {
        if (representativeCustomMetricVo instanceof IntCountMetricVo) {
            return map(
                    createIntCountMetric(metricName, customMetricVos)
            );
        }
        if (representativeCustomMetricVo instanceof LongCountMetricVo) {
            return map(
                    createLongCountMetric(metricName, customMetricVos)
            );
        }
        if (representativeCustomMetricVo instanceof IntGaugeMetricVo) {
            return map(
                    createIntGaugeMetric(metricName, customMetricVos)
            );
        }
        if (representativeCustomMetricVo instanceof LongGaugeMetricVo) {
            return map(
                    createLongGaugeMetric(metricName, customMetricVos)
            );
        }
        if (representativeCustomMetricVo instanceof DoubleGaugeMetricVo) {
            return map(
                    createDoubleGaugeMetric(metricName, customMetricVos)
            );
        }
        return null;
    }


    @Mappings({
            @Mapping(source = ".", target = "intCountMetric"),
            @Mapping(target = "unknownFields", ignore = true),
            @Mapping(target = "allFields", ignore = true),
    })
    PCustomMetric map(PIntCountMetric intCountMetric);

    @Mappings({
            @Mapping(source = "metricName", target = "name"),
    })
    PIntCountMetric createIntCountMetric(String metricName, CustomMetricVo[] customMetricVos);

    @Mappings({
            @Mapping(source = ".", target = "longCountMetric"),
            @Mapping(target = "unknownFields", ignore = true),
            @Mapping(target = "allFields", ignore = true),
    })
    PCustomMetric map(PLongCountMetric pLongCountMetric);

    @Mappings({
            @Mapping(source = "metricName", target = "name"),
    })
    PLongCountMetric createLongCountMetric(String metricName, CustomMetricVo[] customMetricVos);

    @Mappings({
            @Mapping(source = ".", target = "intGaugeMetric"),
            @Mapping(target = "unknownFields", ignore = true),
            @Mapping(target = "allFields", ignore = true),
    })
    PCustomMetric map(PIntGaugeMetric pIntGaugeMetric);

    @Mappings({
            @Mapping(source = "metricName", target = "name"),
    })
    PIntGaugeMetric createIntGaugeMetric(String metricName, CustomMetricVo[] customMetricVos);

    @Mappings({
            @Mapping(source = ".", target = "longGaugeMetric"),
            @Mapping(target = "unknownFields", ignore = true),
            @Mapping(target = "allFields", ignore = true),
    })
    PCustomMetric map(PLongGaugeMetric pLongGaugeMetric);

    @Mappings({
            @Mapping(source = "metricName", target = "name"),
    })
    PLongGaugeMetric createLongGaugeMetric(String metricName, CustomMetricVo[] customMetricVos);

    @Mappings({
            @Mapping(source = ".", target = "doubleGaugeMetric"),
            @Mapping(target = "unknownFields", ignore = true),
            @Mapping(target = "allFields", ignore = true),
    })
    PCustomMetric map(PDouleGaugeMetric pDouleGaugeMetric);

    @Mappings({
            @Mapping(source = "metricName", target = "name"),
    })
    PDouleGaugeMetric createDoubleGaugeMetric(String metricName, CustomMetricVo[] customMetricVos);


    class Holder<V> {
        V value;

        public Holder(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    @AfterMapping
    default void mapIntCountMetricVo(
            String metricName, CustomMetricVo[] customMetricVos, @MappingTarget PIntCountMetric.Builder builder
    ) {
        Holder<Integer> prevValue = new Holder<>(0);
        for (CustomMetricVo customMetricVo : customMetricVos) {
            builder.addValues(toIntCountValue(customMetricVo, prevValue));
        }
    }

    @AfterMapping
    default void mapLongCountMetricVo(
            String metricName, CustomMetricVo[] customMetricVos, @MappingTarget PLongCountMetric.Builder builder
    ) {
        Holder<Long> prevValue = new Holder<>((long) 0);
        for (CustomMetricVo customMetricVo : customMetricVos) {
            builder.addValues(toLongCountValue(customMetricVo, prevValue));
        }
    }

    @AfterMapping
    default void mapIntGaugeMetricVo(
            String metricName, CustomMetricVo[] customMetricVos, @MappingTarget PIntGaugeMetric.Builder builder
    ) {
        for (CustomMetricVo customMetricVo : customMetricVos) {
            builder.addValues(toIntGaugeValue(customMetricVo));
        }
    }

    @AfterMapping
    default void mapLongGaugeMetricVo(
            String metricName, CustomMetricVo[] customMetricVos, @MappingTarget PLongGaugeMetric.Builder builder
    ) {
        for (CustomMetricVo customMetricVo : customMetricVos) {
            builder.addValues(toLongGaugeValue(customMetricVo));
        }
    }

    @AfterMapping
    default void mapDoubleGaugeMetricVo(
            String metricName, CustomMetricVo[] customMetricVos, @MappingTarget PDouleGaugeMetric.Builder builder
    ) {
        for (CustomMetricVo customMetricVo : customMetricVos) {
            builder.addValues(toDoubleGaugeValue(customMetricVo));
        }
    }

    default PIntValue toIntCountValue(CustomMetricVo customMetricVo, Holder<Integer> prev) {
        if (customMetricVo instanceof IntCountMetricVo) {
            int value = ((IntCountMetricVo) customMetricVo).getValue();
            PIntValue intValue = createIntValue(value - prev.getValue());
            prev.setValue(value);
            return intValue;
        }
        return createNotSetIntValue();
    }

    default PLongValue toLongCountValue(CustomMetricVo customMetricVo, Holder<Long> prev) {
        if (customMetricVo instanceof LongCountMetricVo) {
            long value = ((LongCountMetricVo) customMetricVo).getValue();
            PLongValue longValue = createLongValue(value - prev.getValue());
            prev.setValue(value);
            return longValue;
        }
        return createNotSetLongValue();
    }

    default PIntValue toIntGaugeValue(CustomMetricVo customMetricVo) {
        if (customMetricVo instanceof IntGaugeMetricVo) {
            int value = ((IntGaugeMetricVo) customMetricVo).getValue();
            return createIntValue(value);
        }
        return createNotSetIntValue();
    }

    default PLongValue toLongGaugeValue(CustomMetricVo customMetricVo) {
        if (customMetricVo instanceof LongGaugeMetricVo) {
            long value = ((LongGaugeMetricVo) customMetricVo).getValue();
            return createLongValue(value);
        }
        return createNotSetLongValue();
    }

    default PDoubleValue toDoubleGaugeValue(CustomMetricVo customMetricVo) {
        if (customMetricVo instanceof DoubleGaugeMetricVo) {
            double value = ((DoubleGaugeMetricVo) customMetricVo).getValue();
            return createDoubleValue(value);
        }
        return createNotSetDoubleValue();
    }

    static PIntValue createIntValue(int value) {
        PIntValue.Builder builder = PIntValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    static PIntValue createNotSetIntValue() {
        PIntValue.Builder builder = PIntValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }

    static PLongValue createLongValue(long value) {
        PLongValue.Builder builder = PLongValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    static PLongValue createNotSetLongValue() {
        PLongValue.Builder builder = PLongValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }

    static PDoubleValue createDoubleValue(double value) {
        PDoubleValue.Builder builder = PDoubleValue.newBuilder();
        builder.setValue(value);
        return builder.build();
    }

    static PDoubleValue createNotSetDoubleValue() {
        PDoubleValue.Builder builder = PDoubleValue.newBuilder();
        builder.setIsNotSet(true);
        return builder.build();
    }
}
