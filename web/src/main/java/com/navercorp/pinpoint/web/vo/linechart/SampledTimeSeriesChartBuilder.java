package com.nhn.pinpoint.web.vo.linechart;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.vo.linechart.Chart.Point;
import com.nhn.pinpoint.web.vo.linechart.Chart.Points;

/**
 * @author hyungil.jeong
 */
public abstract class SampledTimeSeriesChartBuilder<Y extends Number> extends SampledChartBuilder<Long, Y> {

    private final TimeWindow timeWindow;
    private final Y defaultValue;
    private final List<List<Y>> timeslots;
    
    protected SampledTimeSeriesChartBuilder(TimeWindow timeWindow, Y defaultValue) {
        this.defaultValue = defaultValue;
        this.timeWindow = timeWindow;
        if (this.timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        int numTimeslots = (int)(long)this.timeWindow.getWindowRangeCount();
        this.timeslots = new ArrayList<List<Y>>(numTimeslots);
        initializeTimeslots(numTimeslots);
    }
    
    private void initializeTimeslots(int numTimeslots) {
        for (int i = 0; i < numTimeslots; ++i) {
            this.timeslots.add(new ArrayList<Y>());
        }
    }
    
    @Override
    protected Points makePoints(List<DataPoint<Long, Y>> dataPoints) {
        Points points = new Points();
        allocateDataPoints(dataPoints);
        int timeSlotIndex = 0;
        for (Long timestamp : this.timeWindow) {
            List<Y> dataPointsToSample = this.timeslots.get(timeSlotIndex);
            points.addPoint(makePoint(timestamp, dataPointsToSample));
            ++timeSlotIndex;
        }
        return points;
    }
    
    private void allocateDataPoints(List<DataPoint<Long, Y>> dataPoints) {
        for (DataPoint<Long, Y> dataPoint : dataPoints) {
            int timeslotIndex = this.timeWindow.getWindowIndex(dataPoint.getxVal());
            if (isValidIndex(timeslotIndex)) {
                List<Y> timeSlottedDataPoints = this.timeslots.get(timeslotIndex);
                timeSlottedDataPoints.add(dataPoint.getyVal());
            }
        }
    }
    
    private Point makePoint(Long xVal, List<Y> sampleBuffer) {
        if (sampleBuffer.isEmpty()) {
            return new Point(xVal, this.defaultValue, this.defaultValue, this.defaultValue);
        } else {
            Y minVal = sampleMin(sampleBuffer);
            Y maxVal = sampleMax(sampleBuffer);
            Y avgVal = sampleAvg(sampleBuffer);
            return new Point(xVal, minVal, maxVal, avgVal);
        }
    }
    
    private boolean isValidIndex(int timeslot) {
        return timeslot >= 0 && timeslot < this.timeslots.size();
    }
    
}
