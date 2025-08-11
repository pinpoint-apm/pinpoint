package com.navercorp.pinpoint.uristat.web.entity;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * @author emeroad
 */
public class UriHistogramTotalEntity extends UriEntity {
    // total
    private double tot;

    public UriHistogramTotalEntity() {
    }

    public double getTot() {
        return tot;
    }

    public void setTot(double tot) {
        this.tot = tot;
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
        return Doubles.asList(this.tot);
    }
}
