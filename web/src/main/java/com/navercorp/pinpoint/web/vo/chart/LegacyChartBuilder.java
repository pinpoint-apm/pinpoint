/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public class LegacyChartBuilder<D extends DataPoint<X, Y>, X extends Number, Y extends Number> {

    private final List<D> dataPoints;

    protected LegacyChartBuilder() {
        this.dataPoints = new ArrayList<>();
    }

    public void addDataPoint(D dataPoint) {
        this.dataPoints.add(dataPoint);
    }

    public Chart<X, Y> buildChart() {
        List<Point<X, Y>> points = makePoints(this.dataPoints);
        return new Chart<>(points);
    }

    protected List<Point<X, Y>> makePoints(List<D> dataPoints) {
        List<Point<X, Y>> points = new ArrayList<>(dataPoints.size());
        for (D dataPoint : dataPoints) {
            points.add(new Point<>(dataPoint.getXVal(), dataPoint.getYVal()));
        }
        return points;
    }

    public int numDataPoints() {
        return this.dataPoints.size();
    }
}
