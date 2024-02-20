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
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class HistogramMapper extends OtlpMetricDataMapper {
    private static final String HISTOGRAM_COUNT_FIELDNAME = "count";
    private static final String HISTOGRAM_SUM_FIELDNAME = "sum";
    private static final String HISTOGRAM_MIN_FIELDNAME = "min";
    private static final String HISTOGRAM_MAX_FIELDNAME = "max";
    private static final String HISTOGRAM_BUCKET_COUNT_FIELDNAME = "numBuckets";

    @Override
    public void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        if (metric.hasHistogram()) {
            builder.setMetricType(MetricType.HISTOGRAM);
            setMetricName(builder, metric.getName());
            String description = metric.getDescription();

            final Histogram histogram = metric.getHistogram();
            builder.setAggreTemporality(histogram.getAggregationTemporality());
            final List<HistogramDataPoint> dataPoints = histogram.getDataPointsList();
            for (HistogramDataPoint data : dataPoints) {
                OtlpMetricDataPoint.Builder dataPointBuilder = new OtlpMetricDataPoint.Builder();
                dataPointBuilder.setFlags(DataPointFlags.forNumber(data.getFlags()));
                dataPointBuilder.setEventTime(data.getTimeUnixNano() / NANO_TO_MS);
                dataPointBuilder.setStartTime(data.getStartTimeUnixNano() / NANO_TO_MS);
                Map<String, String> tags = getTags(data.getAttributesList());

                addHistogramInfo(builder, dataPointBuilder, data.getMin(), data.getMax(), data.getSum(), data.getCount());
                addHistogramBuckets(builder, dataPointBuilder, data.getExplicitBoundsList(), data.getBucketCountsList(), tags, commonTags, description);
            }
        }
    }

    private void addHistogramBuckets(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder,
                                     List<Double> explicitBoundsList, List<Long> bucketCountsList,
                                     Map<String, String> tags, Map<String, String> commonTags, String description) {
        setAggreFunction(AggreFunc.SUM, dataPointBuilder, tags);
        dataPointBuilder.addTags(tags);
        dataPointBuilder.addTags(commonTags);

        int bucketCounts = bucketCountsList.size();
        dataPointBuilder.setFieldName(HISTOGRAM_BUCKET_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.LATEST);
        dataPointBuilder.setDataType(DataType.LONG);
        dataPointBuilder.setValue(bucketCounts);
        dataPointBuilder.setDescription(description);
        builder.addValue(dataPointBuilder.build());

        int bucketBoundsCount = explicitBoundsList.size();
        if ((bucketBoundsCount != bucketCounts - 1) || bucketCounts < 1) {
            return;
        }

        String prev = "-Inf";
        dataPointBuilder.setDescription("");
        dataPointBuilder.setDataType(DataType.LONG);
        for (int index = 0; index < bucketCounts; index++) {
            dataPointBuilder.setValue(bucketCountsList.get(index));
            String upperBound;
            if (index != bucketBoundsCount) {
                upperBound = explicitBoundsList.get(index).toString();;
            } else {
                upperBound = "+Inf";
            }
            dataPointBuilder.setFieldName("(" + prev + ", " + upperBound + "]");
            builder.addValue(dataPointBuilder.build());
            prev = upperBound;
        }
    }

    private void addHistogramInfo(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder,
                                  double min, double max, double sum, long count) {
        dataPointBuilder.setFieldName(HISTOGRAM_MIN_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.MIN);
        dataPointBuilder.setDataType(DataType.DOUBLE);
        dataPointBuilder.setValue(min);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(HISTOGRAM_MAX_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.MAX);
        dataPointBuilder.setValue(max);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(HISTOGRAM_SUM_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(sum);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(HISTOGRAM_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setDataType(DataType.LONG);
        dataPointBuilder.setValue(count);
        builder.addValue(dataPointBuilder.build());
    }
}