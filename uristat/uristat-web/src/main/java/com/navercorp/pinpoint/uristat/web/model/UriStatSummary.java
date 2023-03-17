/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.uristat.web.model;

import com.navercorp.pinpoint.common.util.MathUtils;

public class UriStatSummary {
    private final String uri;
    private final double totalCount;
    private final double failureCount;
    private final double maxTimeMs;
    private final double avgTimeMs;
    private final double apdex;
    private final int version;

    public UriStatSummary(String uri, double apdexRaw, double totalCount, double failureCount, double maxTimeMs, double totalTimeMs, int version) {
        this.uri = uri;
        this.apdex = MathUtils.average(apdexRaw, totalCount);
        this.totalCount = totalCount;
        this.failureCount = failureCount;
        this.maxTimeMs = maxTimeMs;
        this.avgTimeMs = MathUtils.average(totalTimeMs, totalCount);
        this.version = version;
    }

    @Deprecated
    public UriStatSummary(String uri, double totalCount, double failureCount, double maxTimeMs, double totalTimeMs, int version) {
        this.uri = uri;
        this.apdex = 0;
        this.totalCount = totalCount;
        this.failureCount = failureCount;
        this.maxTimeMs = maxTimeMs;
        this.avgTimeMs = MathUtils.average(totalTimeMs, totalCount);
        this.version = version;
    }
    public String getUri() {
        return uri;
    }

    public double getApdex() {
        return apdex;
    }

    public double getTotalCount() {
        return totalCount;
    }

    public double getFailureCount() {
        return failureCount;
    }

    public double getMaxTimeMs() {
        return maxTimeMs;
    }

    public double getAvgTimeMs() {
        return avgTimeMs;
    }

    public int getVersion() {
        return version;
    }
}
