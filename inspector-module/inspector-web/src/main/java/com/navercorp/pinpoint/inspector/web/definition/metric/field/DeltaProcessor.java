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

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class DeltaProcessor implements FieldPostProcessor {

    @Override
    public List<DataPoint<Double>> postProcess(List<DataPoint<Double>> dataPointList) {
        if (dataPointList.isEmpty()) {
            return dataPointList;
        }

        List<DataPoint<Double>> postProcessedList = new ArrayList<>(dataPointList.size());

        DataPoint<Double> prevPoint = dataPointList.get(0);
        postProcessedList.add(Points.ofDouble(prevPoint.getTimestamp(), 0.0));

        for (int i = 1; i < dataPointList.size(); i++) {
            DataPoint<Double> currentPoint = dataPointList.get(i);
            double prevValue = prevPoint.getValue();
            double currentValue = currentPoint.getValue();
            double deltaValue = currentValue - prevValue;

            if (deltaValue < 0) {
                deltaValue = currentValue;
            }

            DataPoint<Double> deltaDataPoint = Points.ofDouble(currentPoint.getTimestamp(), deltaValue);
            postProcessedList.add(deltaDataPoint);
            prevPoint = currentPoint;
        }

        return postProcessedList;
    }

    @Override
    public String getName() {
        return "delta";
    }
}
