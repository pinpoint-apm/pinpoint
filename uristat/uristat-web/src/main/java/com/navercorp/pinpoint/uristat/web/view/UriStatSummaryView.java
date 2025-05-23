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
package com.navercorp.pinpoint.uristat.web.view;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author intr3p1d
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UriStatSummaryView {

    private String uri;
    private double totalCount;
    private double failureCount;
    private double maxTimeMs;
    private double avgTimeMs;
    private double apdex;
    private String version;

    UriStatView chart;

    public UriStatSummaryView() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(double totalCount) {
        this.totalCount = totalCount;
    }

    public double getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(double failureCount) {
        this.failureCount = failureCount;
    }

    public double getMaxTimeMs() {
        return maxTimeMs;
    }

    public void setMaxTimeMs(double maxTimeMs) {
        this.maxTimeMs = maxTimeMs;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public void setAvgTimeMs(double avgTimeMs) {
        this.avgTimeMs = avgTimeMs;
    }

    public double getApdex() {
        return apdex;
    }

    public void setApdex(double apdex) {
        this.apdex = apdex;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UriStatView getChart() {
        return chart;
    }

    public void setChart(UriStatView chart) {
        this.chart = chart;
    }
}
