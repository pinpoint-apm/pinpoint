package com.navercorp.pinpoint.uristat.web.entity;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * @author emeroad
 */
public class UriHistogramFailEntity extends UriEntity {
    // fail
    private double fail;


    public UriHistogramFailEntity() {
    }

    public double getFail() {
        return fail;
    }

    public void setFail(double fail) {
        this.fail = fail;
    }

    public List<Double> getFailureHistogram() {
        return Doubles.asList(
                this.fail
        );
    }
}
