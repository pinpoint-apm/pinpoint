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
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SummaryMapper extends OtlpMetricDataMapper {
    private static final String SUMMARY_COUNT_FIELDNAME = "count";
    private static final String SUMMARY_SUM_FIELDNAME = "sum";
    private static final String SUMMARY_QUANTILES_COUNT_FIELDNAME = "numQuantiles";

    @Override
    void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        if (metric.hasSummary()) {
            builder.setMetricType(MetricType.SUMMARY);
            setMetricName(builder, metric.getName());

            final Summary summary = metric.getSummary();
            final List<SummaryDataPoint> dataPoints = summary.getDataPointsList();
            for (SummaryDataPoint data : dataPoints) {
                OtlpMetricDataPoint.Builder dataPointBuilder = new OtlpMetricDataPoint.Builder();
                dataPointBuilder.setFlags(DataPointFlags.forNumber(data.getFlags()));
                dataPointBuilder.setEventTime(data.getTimeUnixNano() / NANO_TO_MS);
                dataPointBuilder.setStartTime(data.getStartTimeUnixNano() / NANO_TO_MS);

                Map<String, String> tags = getTags(data.getAttributesList());
                setAggreFunction(AggreFunc.SUM, dataPointBuilder, tags);
                dataPointBuilder.addTags(tags);
                dataPointBuilder.addTags(commonTags);

                addDataPoints(builder, dataPointBuilder, data, metric.getDescription());
            }
        }
    }

    private void addDataPoints(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder, SummaryDataPoint data, String description) {
        dataPointBuilder.setFieldName(SUMMARY_SUM_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(data.getSum());
        dataPointBuilder.setDataType(DataType.DOUBLE);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(SUMMARY_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(data.getCount());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        List<SummaryDataPoint.ValueAtQuantile> quantileValuesList = data.getQuantileValuesList();
        for (int index = 0; index < quantileValuesList.size(); index++) {
            SummaryDataPoint.ValueAtQuantile item = quantileValuesList.get(index);
            dataPointBuilder.setAggreFunc(AggreFunc.LATEST);
            dataPointBuilder.setValue(quantileValuesList.get(index).getValue());
            dataPointBuilder.setDataType(DataType.DOUBLE);
            dataPointBuilder.setFieldName("p" + Math.round(item.getQuantile() * 100));
            builder.addValue(dataPointBuilder.build());

        }
        dataPointBuilder.setFieldName(SUMMARY_QUANTILES_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(quantileValuesList.size());
        dataPointBuilder.setDataType(DataType.LONG);
        dataPointBuilder.setDescription(description);
        builder.addValue(dataPointBuilder.build());
    }

}
