package com.nhn.pinpoint.web.vo.linechart;

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
            this.dataPoints = new ArrayList<DataPoint<X, Y>>();
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
            this.points = new ArrayList<Point>();
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
