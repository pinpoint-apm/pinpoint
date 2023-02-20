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

package com.navercorp.pinpoint.uristat.collector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.uristat.collector.util.StringPrecondition;

import java.util.Arrays;
import java.util.Objects;

public class UriStat {
    private static final String EMPTY_STRING = "";
    private static final long EMPTY_NUMBER = 0L;
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;
    private final double apdexRaw;
    private final long count;
    private final long failCount;
    private final long maxLatencyMs;
    private final long totalTimeMs;
    private final int[] totalHistogram;
    private final int[] failureHistogram;
    private final long timestamp;
    private final int version;

    public UriStat(long timestamp, String tenantId, String serviceName, String applicationName, String agentId, String uri, long maxLatencyMs, long totalTimeMs, int[] totalHistogram, int[] failureHistogram, int version) {
        this.timestamp = timestamp;
        this.tenantId = tenantId;
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.uri = StringPrecondition.requireHasLength(uri, "uri");
        this.maxLatencyMs = maxLatencyMs;
        this.totalTimeMs = totalTimeMs;
        this.totalHistogram = Objects.requireNonNull(totalHistogram, "totalHistogram");
        this.failureHistogram = Objects.requireNonNull(failureHistogram, "totalHistogram");
        this.count = Arrays.stream(totalHistogram).sum();
        this.failCount = Arrays.stream(failureHistogram).sum();
        this.apdexRaw = (totalHistogram[0] + totalHistogram[1] + totalHistogram[2] + totalHistogram[3] + (0.5 * totalHistogram[4]));
        this.version = version;
    }

    @Deprecated
    public UriStat(long timestamp, double tot0, double tot1, double tot2, double tot3,
                   double tot4, double tot5, double tot6, double tot7,
                   double fail0, double fail1, double fail2, double fail3,
                   double fail4, double fail5, double fail6, double fail7, int version) {
        this.timestamp = timestamp;
        this.tenantId = EMPTY_STRING;
        this.serviceName = EMPTY_STRING;
        this.applicationName = EMPTY_STRING;
        this.agentId = EMPTY_STRING;
        this.uri = EMPTY_STRING;
        this.count = EMPTY_NUMBER;
        this.failCount = EMPTY_NUMBER;
        this.maxLatencyMs = EMPTY_NUMBER;
        this.totalTimeMs = EMPTY_NUMBER;
        this.apdexRaw = EMPTY_NUMBER;
        this.totalHistogram = new int[]{(int) tot0, (int) tot1, (int) tot2, (int) tot3, (int) tot4, (int) tot5, (int) tot6, (int) tot7};
        this.failureHistogram = new int[]{(int) fail0, (int) fail1, (int) fail2, (int) fail3, (int) fail4, (int) fail5, (int) fail6, (int) fail7};
        this.version = version;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUri() {
        return uri;
    }

    public double getApdexRaw() {
        return apdexRaw;
    }

    public long getCount() {
        return count;
    }

    public long getFailureCount() {
        return failCount;
    }

    public long getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getVersion() {
        return version;
    }

    public int getTot0() {
        return totalHistogram[0];
    }

    public int getTot1() {
        return totalHistogram[1];
    }

    public int getTot2() {
        return totalHistogram[2];
    }

    public int getTot3() {
        return totalHistogram[3];
    }

    public int getTot4() {
        return totalHistogram[4];
    }

    public int getTot5() {
        return totalHistogram[5];
    }

    public int getTot6() {
        return totalHistogram[6];
    }

    public int getTot7() {
        return totalHistogram[7];
    }

    public int getFail0() {
        return failureHistogram[0];
    }

    public int getFail1() {
        return failureHistogram[1];
    }

    public int getFail2() {
        return failureHistogram[2];
    }

    public int getFail3() {
        return failureHistogram[3];
    }

    public int getFail4() {
        return failureHistogram[4];
    }

    public int getFail5() {
        return failureHistogram[5];
    }

    public int getFail6() {
        return failureHistogram[6];
    }

    public int getFail7() {
        return failureHistogram[7];
    }

    @JsonIgnore
    public int[] getTotalHistogram() {
        return totalHistogram;
    }

    @JsonIgnore
    public int[] getFailureHistogram() {
        return failureHistogram;
    }

    @Override
    public String toString() {
        return "UriStat{" +
                "serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", uri=" + uri +
                ", apdexRaw= " + apdexRaw +
                ", count=" + count +
                ", maxLatencyMs=" + maxLatencyMs +
                ", totalTimeMs=" + totalTimeMs +
                ", totalHistogram=" + Arrays.toString(totalHistogram) +
                ", failureHistogram=" + Arrays.toString(failureHistogram) +
                ", timestamp=" + timestamp +
                '}';
    }
}
