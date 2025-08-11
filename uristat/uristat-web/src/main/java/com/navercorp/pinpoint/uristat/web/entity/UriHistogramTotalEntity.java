package com.navercorp.pinpoint.uristat.web.entity;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * @author emeroad
 */
public class UriHistogramTotalEntity extends UriEntity {
    // total
    private double tot0;
    private double tot1;
    private double tot2;
    private double tot3;
    private double tot4;
    private double tot5;
    private double tot6;
    private double tot7;

    public UriHistogramTotalEntity() {
    }

    public double getTot0() {
        return tot0;
    }

    public void setTot0(double tot0) {
        this.tot0 = tot0;
    }

    public double getTot1() {
        return tot1;
    }

    public void setTot1(double tot1) {
        this.tot1 = tot1;
    }

    public double getTot2() {
        return tot2;
    }

    public void setTot2(double tot2) {
        this.tot2 = tot2;
    }

    public double getTot3() {
        return tot3;
    }

    public void setTot3(double tot3) {
        this.tot3 = tot3;
    }

    public double getTot4() {
        return tot4;
    }

    public void setTot4(double tot4) {
        this.tot4 = tot4;
    }

    public double getTot5() {
        return tot5;
    }

    public void setTot5(double tot5) {
        this.tot5 = tot5;
    }

    public double getTot6() {
        return tot6;
    }

    public void setTot6(double tot6) {
        this.tot6 = tot6;
    }

    public double getTot7() {
        return tot7;
    }

    public void setTot7(double tot7) {
        this.tot7 = tot7;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Double> getTotalHistogram() {
        return Doubles.asList(this.tot0, this.tot1, this.tot2, this.tot3,
                this.tot4, this.tot5, this.tot6, this.tot7);
    }
}
