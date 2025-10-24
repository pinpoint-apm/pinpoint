/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
@Component
public class AvgUsingIntervalForTotalCountPostProcessor extends AvgUsingIntervalPostProcessor {

    private static final String TOTAL_COUNT_FIELD = "totalCount";

    @Override
    public String getName() {
        return "avgUsingCollectIntervalForTotalCount";
    }

    @Override
    public List<InspectorMetricValue> postProcess(List<InspectorMetricValue> metricValueList) {
        List<InspectorMetricValue> processedMetricValueList = new ArrayList<>(metricValueList.size() - 1);

        InspectorMetricValue avgTotalCount = calculateAvg(metricValueList);
        processedMetricValueList.add(avgTotalCount);

        List<InspectorMetricValue> otherMetricValueList = extractOtherMetric(metricValueList);
        processedMetricValueList.addAll(otherMetricValueList);

        return processedMetricValueList;
    }

    private List<InspectorMetricValue> extractOtherMetric(List<InspectorMetricValue> metricValueList) {
        return metricValueList.stream()
                .filter(metricValue -> !TOTAL_COUNT_FIELD.equals(metricValue.getFieldName()) && !COLLECT_INTERVAL_FIELD.equals(metricValue.getFieldName()))
                .collect(Collectors.toList());
    }


    private InspectorMetricValue calculateAvg(List<InspectorMetricValue> metricValueList) {
        InspectorMetricValue collectInterval = findCollectInterval(metricValueList);
        InspectorMetricValue totalCount = extractTotalCount(metricValueList);
        List<Double> valueList = calculateAvg(collectInterval, totalCount);
        return new InspectorMetricValue("AVG", totalCount.getTagList(), "spline", totalCount.getUnit(), valueList);
    }

    private InspectorMetricValue extractTotalCount(List<InspectorMetricValue> metricValueList) {
        return metricValueList.stream()
                .filter(metricValue -> TOTAL_COUNT_FIELD.equals(metricValue.getFieldName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("not found totalCount"));
    }
}
