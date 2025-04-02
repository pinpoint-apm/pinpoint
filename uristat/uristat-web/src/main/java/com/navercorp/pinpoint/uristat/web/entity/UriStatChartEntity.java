/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.uristat.web.entity;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * @author intr3p1d
 */
public class UriStatChartEntity extends UriEntity {

    // total
    private double tot0;
    private double tot1;
    private double tot2;
    private double tot3;
    private double tot4;
    private double tot5;
    private double tot6;
    private double tot7;

    // fail
    private double fail0;
    private double fail1;
    private double fail2;
    private double fail3;
    private double fail4;
    private double fail5;
    private double fail6;
    private double fail7;

    // latency
    private double totalTimeMs;
    private double maxLatencyMs;
    private double count;

    // apdex
    private double apdexRaw;


    public UriStatChartEntity() {
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

    public double getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(double totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public double getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(double maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getApdexRaw() {
        return apdexRaw;
    }

    public void setApdexRaw(double apdexRaw) {
        this.apdexRaw = apdexRaw;
    }

    public List<Double> getTotalHistogram() {
        return Doubles.asList(
                this.tot0, this.tot1, this.tot2, this.tot3,
                this.tot4, this.tot5, this.tot6, this.tot7
        );
    }

    public List<Double> getFailureHistogram() {
        return Doubles.asList(
                this.fail0, this.fail1, this.fail2, this.fail3,
                this.fail4, this.fail5, this.fail6, this.fail7
        );
    }
}
