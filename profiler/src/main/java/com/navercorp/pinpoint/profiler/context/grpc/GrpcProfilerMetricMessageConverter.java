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
package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PProfilerMetric;
import com.navercorp.pinpoint.grpc.trace.PProfilerMetricField;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.monitor.metric.profilermetric.Field;
import com.navercorp.pinpoint.profiler.monitor.metric.profilermetric.NetworkMetric;

import java.util.List;

public class GrpcProfilerMetricMessageConverter implements MessageConverter<MetricType, PProfilerMetric> {
    @Override
    public PProfilerMetric toMessage(MetricType message) {
        if (message instanceof NetworkMetric) {
            return convertNetworkMetric((NetworkMetric) message);
        } else {
            return null;
        }
    }

    private PProfilerMetric convertNetworkMetric(NetworkMetric message) {
        PProfilerMetric.Builder builder = PProfilerMetric.newBuilder();
        builder.setTimestamp(message.getTimestamp());
        builder.setCollectInterval(message.getCollectInterval());
        builder.setName(message.getName());
        addFields(builder, message.getFields());
        addTags(builder, message.getTags());
        return builder.build();
    }

    private void addFields(PProfilerMetric.Builder builder, List<Field<Long>> fields) {
        for(Field<Long> f : fields) {
            PProfilerMetricField.Builder fieldBuilder = PProfilerMetricField.newBuilder();
            fieldBuilder.setName(f.getName());
            fieldBuilder.setLongValue(f.getValue());
            builder.addFields(fieldBuilder);
        }
    }

    private void addTags(PProfilerMetric.Builder builder, List<Field<String>> fields) {
        for(Field<String> f : fields) {
            PProfilerMetricField.Builder fieldBuilder = PProfilerMetricField.newBuilder();
            fieldBuilder.setName(f.getName());
            fieldBuilder.setStringValue(f.getValue());
            builder.addTags(fieldBuilder);
        }
    }
}
