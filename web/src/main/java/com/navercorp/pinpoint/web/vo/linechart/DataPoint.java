package com.nhn.pinpoint.web.vo.linechart;

/**
 * @author hyungil.jeong
 */
public class DataPoint<X extends Number, Y extends Number> {

    private final X xVal;
    private final Y yVal;

    public DataPoint(X xVal, Y yVal) {
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public X getxVal() {
        return xVal;
    }

    public Y getyVal() {
        return yVal;
    }

    @Override
    public String toString() {
        return "(" + xVal + "," + yVal + ")";
    }
    
}
