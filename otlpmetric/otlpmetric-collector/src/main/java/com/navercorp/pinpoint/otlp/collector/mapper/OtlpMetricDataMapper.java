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
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class OtlpMetricDataMapper {
    protected static final String CUSTOM_AGGRE_FUNCTION_KEY = "pinpoint.metric.aggregation";
    protected static final long NANO_TO_MS = 1000000;

    abstract void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags);

    protected String setMetricName(OtlpMetricData.Builder builder, String metricName) {
        List<String> names = new LinkedList<>(Arrays.asList(metricName.split("\\.")));
        int length = names.size();

        if ( length == 1 ) {
            builder.setMetricName(names.get(length - 1));
        } else {
            builder.setMetricName(names.remove(length - 1));
            builder.setMetricGroupName(String.join(".", names));
        }

        return "";
    }

    protected String resolveTagValue(AnyValue value) {
        if (value.hasStringValue()) {
            return value.getStringValue();
        } else if (value.hasIntValue()) {
            return String.valueOf(value.getIntValue());
        } else if (value.hasDoubleValue()) {
            return String.valueOf(value.getDoubleValue());
        } else if (value.hasBoolValue()) {
            return String.valueOf(value.getBoolValue());
        } else if (value.hasKvlistValue()) {
            return String.valueOf(value.getKvlistValue());
        } else {
            return "Unsupported value type.";
        }
    }

    protected Map<String, String> getTags(List<KeyValue> attributes) {
        Map<String, String> tags = new HashMap<>();
        for(KeyValue keyValue : attributes) {
            String key = keyValue.getKey();
            tags.put(key, resolveTagValue(keyValue.getValue()));
        }
        return tags;
    }

    protected void setAggreFunction(AggreFunc defaultAggreFunc, OtlpMetricDataPoint.Builder dataPointBuilder, Map<String, String> tags) {
        if (tags.containsKey(CUSTOM_AGGRE_FUNCTION_KEY)) {
            String value = tags.remove(CUSTOM_AGGRE_FUNCTION_KEY);
            AggreFunc aggreFunc = AggreFunc.forName(value);
            if (aggreFunc != null) {
                dataPointBuilder.setAggreFunc(aggreFunc);
                return;
            }
        }
        dataPointBuilder.setAggreFunc(defaultAggreFunc);
    }
}
