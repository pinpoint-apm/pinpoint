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
import java.util.Collections;
import java.util.List;

public class Chart {

    private final Points points;

    private Chart(Points points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return this.points.getPoints();
    }

    public static abstract class ChartBuilder<X extends Number, Y extends Number> {

        protected abstract Points makePoints(List<DataPoint<X, Y>> dataPoints);

        private final List<DataPoint<X, Y>> dataPoints;

        protected ChartBuilder() {
            this.dataPoints = new ArrayList<>();
        }

        public void addDataPoint(DataPoint<X, Y> dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        public Chart buildChart() {
            Points points = makePoints(this.dataPoints);
            return new Chart(points);
        }

        public int numDataPoints() {
            return this.dataPoints.size();
        }

    }

    static final class Points {

        private final List<Point> points;

        public Points() {
            this.points = new ArrayList<>();
        }

        public void addPoint(Point point) {
            this.points.add(point);
        }

        public List<Point> getPoints() {
            return Collections.unmodifiableList(this.points);
        }

    }

    public static final class Point {

        private final Number timestamp;
        private final Number minVal;
        private final Number maxVal;
        private final Number avgVal;

        public Point(Number timestamp, Number minVal, Number maxVal, Number avgVal) {
            this.timestamp = timestamp;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.avgVal = avgVal;
        }

        public Number getTimestamp() {
            return timestamp;
        }

        public Number getMinVal() {
            return minVal;
        }

        public Number getMaxVal() {
            return maxVal;
        }

        public Number getAvgVal() {
            return avgVal;
        }

        @Override
        public String toString() {
            return "Point [timestamp=" + timestamp + ", minVal=" + minVal + ", maxVal=" + maxVal + ", avgVal=" + avgVal + "]";
        }

    }
}
