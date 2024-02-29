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

package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
@Component
public class AvgUsingIntervalPostProcessor implements MetricPostProcessor {

    private static final int NUM_DECIMAL_PLACES = 1;
    protected static final String COLLECT_INTERVAL_FIELD = "collectInterval";

    @Override
    public String getName() {
        return "avgUsingCollectInterval";
    }

    @Override
    public List<InspectorMetricValue> postProcess(List<InspectorMetricValue> metricValueList) {
        InspectorMetricValue collectInterval = findCollectInterval(metricValueList);
        List<InspectorMetricValue> metricCountList = extractMetricCount(metricValueList);
        List<InspectorMetricValue> processedMetricValueList = new ArrayList<>();


        calculateAvg(collectInterval, metricCountList, processedMetricValueList);
        addTotalMetricValue(processedMetricValueList, collectInterval.getValueList().size());

        return processedMetricValueList;
    }

    private void addTotalMetricValue(List<InspectorMetricValue> processedMetricValueList, int metricValueSize) {

        List<Double> totalValueList = new ArrayList<>(metricValueSize);
        for (int i = 0; i < metricValueSize; i++) {
            double total = 0;
            for (InspectorMetricValue metricValue : processedMetricValueList) {
                total += metricValue.getValueList().get(i);
            }
            total = Precision.round(total, NUM_DECIMAL_PLACES);
            totalValueList.add(total);
        }

        processedMetricValueList.add(new InspectorMetricValue("totalCount", Collections.emptyList(), "tooltip", "count", totalValueList));
    }

    private void calculateAvg(InspectorMetricValue collectInterval, List<InspectorMetricValue> metricCountList, List<InspectorMetricValue> processedMetricValueList) {
        for (InspectorMetricValue metricCount : metricCountList) {
            List<Double> valueList = calculateAvg(collectInterval, metricCount);
            processedMetricValueList.add(new InspectorMetricValue(metricCount.getFieldName(), metricCount.getTagList(), "areaSpline", metricCount.getUnit(), valueList));
        }
    }

    protected List<Double> calculateAvg(InspectorMetricValue collectInterval, InspectorMetricValue metricCount) {
        List<Double> valueList = metricCount.getValueList();
        List<Double> commitIntervalList = collectInterval.getValueList();
        List<Double> avgList = new ArrayList<>(valueList.size());
        for (int i = 0; i < valueList.size(); i++) {
            if (commitIntervalList.get(i) < 0) {
                avgList.add(i, -1.0);
                continue;
            }
            avgList.add(calculateTps(valueList.get(i), commitIntervalList.get(i)));
        }

        return avgList;
    }

    private double calculateTps(double count, double intervalMs) {
        return Precision.round(count / (intervalMs / 1000D), NUM_DECIMAL_PLACES);
    }

    private List<InspectorMetricValue> extractMetricCount(List<InspectorMetricValue> metricValueList) {
        return metricValueList.stream()
                .filter(metricValue -> !metricValue.getFieldName().equals(COLLECT_INTERVAL_FIELD))
                .collect(Collectors.toList());
    }

    protected InspectorMetricValue findCollectInterval(List<InspectorMetricValue> metricValueList) {
        return metricValueList.stream()
                .filter(metricValue -> metricValue.getFieldName().equals(COLLECT_INTERVAL_FIELD))
                .findFirst()
                .orElse(null);
    }
}
