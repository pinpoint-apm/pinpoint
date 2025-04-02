/*
 * Copyright 2024 NAVER Corp.
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

/**
 * @author intr3p1d
 */
public class UriStatSummaryEntity extends UriStatChartEntity {
    private String uri;
    private double totalApdexRaw;
    private double totalCount;
    private double failureCount;
    private double maxTimeMs;
    private double sumOfTotalTimeMs;

    public UriStatSummaryEntity() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getTotalApdexRaw() {
        return totalApdexRaw;
    }

    public void setTotalApdexRaw(double totalApdexRaw) {
        this.totalApdexRaw = totalApdexRaw;
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

    public double getSumOfTotalTimeMs() {
        return sumOfTotalTimeMs;
    }

    public void setSumOfTotalTimeMs(double totalTimeMs) {
        this.sumOfTotalTimeMs = totalTimeMs;
    }
}
