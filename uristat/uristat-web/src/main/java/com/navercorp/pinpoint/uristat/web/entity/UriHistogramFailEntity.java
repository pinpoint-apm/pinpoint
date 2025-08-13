package com.navercorp.pinpoint.uristat.web.entity;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * @author emeroad
 */
public class UriHistogramFailEntity extends UriEntity {
    // fail
    private double fail0;
    private double fail1;
    private double fail2;
    private double fail3;
    private double fail4;
    private double fail5;
    private double fail6;
    private double fail7;


    public UriHistogramFailEntity() {
    }

    public double getFail0() {
        return fail0;
    }

    public void setFail0(double fail0) {
        this.fail0 = fail0;
    }

    public double getFail1() {
        return fail1;
    }

    public void setFail1(double fail1) {
        this.fail1 = fail1;
    }

    public double getFail2() {
        return fail2;
    }

    public void setFail2(double fail2) {
        this.fail2 = fail2;
    }

    public double getFail3() {
        return fail3;
    }

    public void setFail3(double fail3) {
        this.fail3 = fail3;
    }

    public double getFail4() {
        return fail4;
    }

    public void setFail4(double fail4) {
        this.fail4 = fail4;
    }

    public double getFail5() {
        return fail5;
    }

    public void setFail5(double fail5) {
        this.fail5 = fail5;
    }

    public double getFail6() {
        return fail6;
    }

    public void setFail6(double fail6) {
        this.fail6 = fail6;
    }

    public double getFail7() {
        return fail7;
    }

    public void setFail7(double fail7) {
        this.fail7 = fail7;
    }

    public List<Double> getFailureHistogram() {
        return Doubles.asList(
                this.fail0, this.fail1, this.fail2, this.fail3,
                this.fail4, this.fail5, this.fail6, this.fail7
        );
    }
}
