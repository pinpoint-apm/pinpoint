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
    private double tot;

    // fail
    private double fail;

    // latency
    private double totalTimeMs;
    private double maxLatencyMs;
    private double count;

    // apdex
    private double apdexRaw;


    public UriStatChartEntity() {
    }

    public double getTot() {
        return tot;
    }

    public void setTot(double tot) {
        this.tot = tot;
    }

    public double getFail() {
        return fail;
    }

    public void setFail(double fail) {
        this.fail = fail;
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
                this.tot
        );
    }

    public List<Double> getFailureHistogram() {
        return Doubles.asList(
                this.fail
        );
    }
}
