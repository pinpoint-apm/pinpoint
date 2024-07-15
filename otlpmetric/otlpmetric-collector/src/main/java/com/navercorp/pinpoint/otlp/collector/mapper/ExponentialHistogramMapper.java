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
import io.opentelemetry.proto.metrics.v1.ExponentialHistogram;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ExponentialHistogramMapper extends OtlpMetricDataMapper {
    private static final String EXP_HISTOGRAM_COUNT_FIELDNAME = "count";
    private static final String EXP_HISTOGRAM_SUM_FIELDNAME = "sum";
    private static final String EXP_HISTOGRAM_SCALE_FIELDNAME = "scale";
    private static final String EXP_HISTOGRAM_ZEROCOUNT_FIELDNAME = "zeroCount";
    private static final String EXP_HISTOGRAM_MIN_FIELDNAME = "min";
    private static final String EXP_HISTOGRAM_MAX_FIELDNAME = "max";
    private static final String EXP_HISTOGRAM_POSITIVE_BUCKET_OFFSET_FIELDNAME = "positiveBucketsOffset";
    private static final String EXP_HISTOGRAM_POSITIVE_BUCKET_COUNT_FIELDNAME = "numPositiveBuckets";
    private static final String EXP_HISTOGRAM_POSITIVE_BUCKET_PREFIX = "+";
    private static final String EXP_HISTOGRAM_NEGATIVE_BUCKET_OFFSET_FIELDNAME = "negativeBucketsOffset";
    private static final String EXP_HISTOGRAM_NEGATIVE_BUCKET_COUNT_FIELDNAME = "numNegativeBuckets";
    private static final String EXP_HISTOGRAM_NEGATIVE_BUCKET_PREFIX = "-";


    @Override
    void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        if (metric.hasExponentialHistogram()) {
            builder.setMetricType(MetricType.EXP_HISTOGRAM);
            setMetricName(builder, metric.getName());

            final ExponentialHistogram histogram = metric.getExponentialHistogram();
            builder.setAggreTemporality(histogram.getAggregationTemporality());

            final List<ExponentialHistogramDataPoint> dataPoints = histogram.getDataPointsList();
            for (ExponentialHistogramDataPoint data : dataPoints) {
                OtlpMetricDataPoint.Builder dataPointBuilder = new OtlpMetricDataPoint.Builder();
                dataPointBuilder.setFlags(DataPointFlags.forNumber(data.getFlags()));
                dataPointBuilder.setEventTime(data.getTimeUnixNano() / NANO_TO_MS);
                dataPointBuilder.setStartTime(data.getStartTimeUnixNano() / NANO_TO_MS);

                Map<String, String> tags = getTags(data.getAttributesList());
                setAggreFunction(AggreFunc.SUM, dataPointBuilder, tags);
                dataPointBuilder.addTags(tags);
                dataPointBuilder.addTags(commonTags);

                addPositiveBuckets(builder, dataPointBuilder, data.getPositive());
                addNegativeBuckets(builder, dataPointBuilder, data.getNegative());
                addExpHistogramInfo(builder, dataPointBuilder, data, metric.getDescription());
            }
        }
    }

    private void addPositiveBuckets(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder, ExponentialHistogramDataPoint.Buckets positive) {
        dataPointBuilder.setFieldName(EXP_HISTOGRAM_POSITIVE_BUCKET_OFFSET_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.LATEST);
        dataPointBuilder.setValue(positive.getOffset());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        List<Long> bucketCountsList = positive.getBucketCountsList();
        dataPointBuilder.setFieldName(EXP_HISTOGRAM_POSITIVE_BUCKET_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(bucketCountsList.size());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        for (int index = 0; index < bucketCountsList.size(); index++) {
            dataPointBuilder.setFieldName(EXP_HISTOGRAM_POSITIVE_BUCKET_PREFIX + index);
            dataPointBuilder.setAggreFunc(AggreFunc.SUM);
            dataPointBuilder.setValue(bucketCountsList.get(index));
            dataPointBuilder.setDataType(DataType.LONG);
            builder.addValue(dataPointBuilder.build());
        }
    }


    private void addNegativeBuckets(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder, ExponentialHistogramDataPoint.Buckets negative) {
        dataPointBuilder.setFieldName(EXP_HISTOGRAM_NEGATIVE_BUCKET_OFFSET_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.LATEST);
        dataPointBuilder.setValue(negative.getOffset());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        List<Long> bucketCountsList = negative.getBucketCountsList();
        dataPointBuilder.setFieldName(EXP_HISTOGRAM_NEGATIVE_BUCKET_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(bucketCountsList.size());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        for (int index = 0; index < bucketCountsList.size(); index++) {
            dataPointBuilder.setFieldName(EXP_HISTOGRAM_NEGATIVE_BUCKET_PREFIX + index);
            dataPointBuilder.setAggreFunc(AggreFunc.SUM);
            dataPointBuilder.setValue(bucketCountsList.get(index));
            dataPointBuilder.setDataType(DataType.LONG);
            builder.addValue(dataPointBuilder.build());
        }
    }

    private void addExpHistogramInfo(OtlpMetricData.Builder builder, OtlpMetricDataPoint.Builder dataPointBuilder, ExponentialHistogramDataPoint data, String description) {
        dataPointBuilder.setFieldName(EXP_HISTOGRAM_MAX_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.MAX);
        dataPointBuilder.setValue(data.getMax());
        dataPointBuilder.setDataType(DataType.DOUBLE);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(EXP_HISTOGRAM_MIN_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.MIN);
        dataPointBuilder.setValue(data.getMin());
        dataPointBuilder.setDataType(DataType.DOUBLE);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(EXP_HISTOGRAM_ZEROCOUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(data.getZeroCount());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(EXP_HISTOGRAM_SUM_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(data.getSum());
        dataPointBuilder.setDataType(DataType.DOUBLE);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(EXP_HISTOGRAM_COUNT_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.SUM);
        dataPointBuilder.setValue(data.getCount());
        dataPointBuilder.setDataType(DataType.LONG);
        builder.addValue(dataPointBuilder.build());

        dataPointBuilder.setFieldName(EXP_HISTOGRAM_SCALE_FIELDNAME);
        dataPointBuilder.setAggreFunc(AggreFunc.LATEST);
        dataPointBuilder.setValue(data.getScale());
        dataPointBuilder.setDataType(DataType.LONG);
        dataPointBuilder.setDescription(description);
        builder.addValue(dataPointBuilder.build());
    }
}
