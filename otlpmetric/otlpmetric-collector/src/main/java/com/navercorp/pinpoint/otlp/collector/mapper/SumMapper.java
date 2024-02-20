/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricDataPoint;
import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import io.opentelemetry.proto.metrics.v1.DataPointFlags;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.Sum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class SumMapper extends OtlpMetricDataMapper {

    @Override
    public void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        if (metric.hasSum()) {
            builder.setMetricType(MetricType.SUM);
            String fieldName = setMetricName(builder, metric.getName());

            final Sum sum = metric.getSum();
            builder.setAggreTemporality(sum.getAggregationTemporality());

            final List<NumberDataPoint> dataPoints = sum.getDataPointsList();
            for (NumberDataPoint data : dataPoints) {
                OtlpMetricDataPoint.Builder dataPointBuilder = new OtlpMetricDataPoint.Builder();
                dataPointBuilder.setFieldName(fieldName);
                dataPointBuilder.setDescription(metric.getDescription());
                dataPointBuilder.setFlags(DataPointFlags.forNumber(data.getFlags()));
                dataPointBuilder.setEventTime(data.getTimeUnixNano() / NANO_TO_MS);
                dataPointBuilder.setStartTime(data.getStartTimeUnixNano() / NANO_TO_MS);

                Map<String, String> tags = getTags(data.getAttributesList());
                setAggreFunction(AggreFunc.SUM, dataPointBuilder, tags);
                dataPointBuilder.addTags(tags);
                dataPointBuilder.addTags(commonTags);

                if (data.hasAsDouble()) {
                    dataPointBuilder.setDataType(DataType.DOUBLE);
                    dataPointBuilder.setValue(data.getAsDouble());
                } else {
                    dataPointBuilder.setDataType(DataType.LONG);
                    dataPointBuilder.setValue(data.getAsInt());
                }
                builder.addValue(dataPointBuilder.build());
            }
        }
    }

    @Override
    protected String setMetricName(OtlpMetricData.Builder builder, String metricName) {
        List<String> names = new LinkedList<>(Arrays.asList(metricName.split("\\.")));
        int length = names.size();

        if ( length == 1 ) {
            builder.setMetricName(names.get(length - 1));
            return "";
        } else if ( length == 2 ) {
            builder.setMetricName(names.get(length - 1));
            builder.setMetricGroupName(names.get(length - 2));
            return "";
        } else {
            String fieldName = names.remove(length - 1);
            builder.setMetricName(names.remove(length - 2));
            builder.setMetricGroupName(String.join(".", names));
            return fieldName;
        }
    }
}
