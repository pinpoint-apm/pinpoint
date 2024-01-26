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

package com.navercorp.pinpoint.inspector.web.definition.metric.field;

import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class DeltaProcessor implements FieldPostProcessor {

    @Override
    public List<SystemMetricPoint<Double>> postProcess(List<SystemMetricPoint<Double>> systemMetricPointList) {
        if (systemMetricPointList.isEmpty()) {
            return systemMetricPointList;
        }

        List<SystemMetricPoint<Double>> postProcessedList = new ArrayList<>(systemMetricPointList.size());

        SystemMetricPoint<Double> prevPoint = systemMetricPointList.get(0);
        postProcessedList.add(new SystemMetricPoint<>(prevPoint.getXVal(), 0.0));

        for (int i = 1; i < systemMetricPointList.size(); i++) {
            SystemMetricPoint<Double> currentPoint = systemMetricPointList.get(i);
            double prevValue = prevPoint.getYVal();
            double currentValue = currentPoint.getYVal();
            double deltaValue = currentValue - prevValue;

            if (deltaValue < 0) {
                deltaValue = currentValue;
            }

            SystemMetricPoint<Double> deltaSystemMetricPoint = new SystemMetricPoint<>(currentPoint.getXVal(), deltaValue);
            postProcessedList.add(deltaSystemMetricPoint);
            prevPoint = currentPoint;
        }

        return postProcessedList;
    }

    @Override
    public String getName() {
        return "delta";
    }
}
