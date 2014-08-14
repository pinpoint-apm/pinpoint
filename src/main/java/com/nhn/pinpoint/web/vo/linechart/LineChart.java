package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

public abstract class LineChart<X extends Number, Y extends Number> extends Chart {

    protected Chart.Points points;

    public LineChart() {
        this.points = new Points();
    }

    public abstract void addPoint(X xVal, Y yVal);

    public void setPoints(Points points) {
        this.points = points;
    }

    public List<Number[]> getPoints() {
        return this.points.getPoints();
    }

}
