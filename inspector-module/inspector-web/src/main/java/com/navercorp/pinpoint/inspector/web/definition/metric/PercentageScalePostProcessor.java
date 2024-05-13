/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.util.DoubleUncollectedDataCreator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
@Component
public class PercentageScalePostProcessor implements MetricPostProcessor {

    private static final double SCALE_FACTOR = 100.0;

    @Override
    public String getName() {
        return "percentageScale";
    }

    @Override
    public List<InspectorMetricValue> postProcess(List<InspectorMetricValue> metricValueList) {
        return metricValueList.stream()
                .map(this::processInspectorMetric)
                .collect(Collectors.toList());
    }

    private InspectorMetricValue processInspectorMetric(InspectorMetricValue inspectorMetric) {
        List<Double> scaledValues = inspectorMetric.getValueList().stream()
                .map(this::scaleToPercentage)
                .collect(Collectors.toList());

        return new InspectorMetricValue(
                inspectorMetric.getFieldName(),
                inspectorMetric.getTagList(),
                inspectorMetric.getChartType(),
                inspectorMetric.getUnit(),
                scaledValues
        );
    }

    private Double scaleToPercentage(Double value) {
        if (value == DoubleUncollectedDataCreator.UNCOLLECTED_VALUE) {
            return value;
        } else {
            return value * SCALE_FACTOR;
        }
    }
}
