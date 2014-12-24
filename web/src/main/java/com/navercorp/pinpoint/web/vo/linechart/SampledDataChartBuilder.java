/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.linechart;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.web.vo.linechart.Chart.Point;
import com.navercorp.pinpoint.web.vo.linechart.Chart.Points;

/**
 * @author hyungil.jeong
 */
public abstract class SampledDataChartBuilder<X extends Number, Y extends Number> extends SampledChartBuilder<X, Y> {
    
    private final int sampleRate;

    protected SampledDataChartBuilder(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    protected Points makePoints(List<DataPoint<X, Y>> dataPoints) {
        Points points = new Points();
        for (int i = 0; i < dataPoints.size(); i += this.sampleRate) {
            final int beginIndex = i;
            final int endIndex = Math.min(beginIndex + this.sampleRate, dataPoints.size());
            List<DataPoint<X, Y>> sampleDataPoints = dataPoints.subList(beginIndex, endIndex);
            points.addPoint(makePoint(sampleDataPoints));
        }
        return points;
    }
    
    private Point makePoint(List<DataPoint<X, Y>> sampleDataPoints) {
        X xVal = sampleDataPoints.get(sampleDataPoints.size()-1).getxVal();
        List<Y> sampleBuffer = new ArrayList<Y>(sampleDataPoints.size());
        for (DataPoint<X, Y> sampleDataPoint : sampleDataPoints) {
            sampleBuffer.add(sampleDataPoint.getyVal());
        }
        Y minVal = sampleMin(sampleBuffer);
        Y maxVal = sampleMax(sampleBuffer);
        Y avgVal = sampleAvg(sampleBuffer);
        return new Point(xVal, minVal, maxVal, avgVal);
    }
    
}
