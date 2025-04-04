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
import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.List;
import java.util.Objects;

public class UriStat {
    private static final long EMPTY_NUMBER = 0L;
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;

    private final long timestamp;

    private final long maxLatencyMs;
    private final long totalTimeMs;

    private final List<Integer> totalHistogram;
    private final List<Integer> failureHistogram;

    private final double apdexRaw;
    private final long count;
    private final long failCount;

    private final int version;

    public UriStat(long timestamp, String tenantId, String serviceName,
                   String applicationName, String agentId,
                   String uri, long maxLatencyMs, long totalTimeMs,
                   List<Integer> totalHistogram,
                   List<Integer> failureHistogram,
                   int version) {
        this.timestamp = timestamp;
        this.tenantId = tenantId;
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.uri = StringPrecondition.requireHasLength(uri, "uri");
        this.maxLatencyMs = maxLatencyMs;
        this.totalTimeMs = totalTimeMs;
        this.totalHistogram = Objects.requireNonNull(totalHistogram, "totalHistogram");
        this.failureHistogram = Objects.requireNonNull(failureHistogram, "failureHistogram");

        this.count = totalHistogram.stream().mapToInt(Integer::intValue).sum();
        this.failCount = failureHistogram.stream().mapToInt(Integer::intValue).sum();
        this.apdexRaw = computeApdexRaw(totalHistogram);

        this.version = version;
    }

    private double computeApdexRaw(List<Integer> totalHistogram) {
        return totalHistogram.get(0) + totalHistogram.get(1) + totalHistogram.get(2) + totalHistogram.get(3)
                + (0.5 * totalHistogram.get(4));
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
        return totalHistogram.get(0);
    }

    public int getTot1() {
        return totalHistogram.get(1);
    }

    public int getTot2() {
        return totalHistogram.get(2);
    }

    public int getTot3() {
        return totalHistogram.get(3);
    }

    public int getTot4() {
        return totalHistogram.get(4);
    }

    public int getTot5() {
        return totalHistogram.get(5);
    }

    public int getTot6() {
        return totalHistogram.get(6);
    }

    public int getTot7() {
        return totalHistogram.get(7);
    }

    public int getFail0() {
        return failureHistogram.get(0);
    }

    public int getFail1() {
        return failureHistogram.get(1);
    }

    public int getFail2() {
        return failureHistogram.get(2);
    }

    public int getFail3() {
        return failureHistogram.get(3);
    }

    public int getFail4() {
        return failureHistogram.get(4);
    }

    public int getFail5() {
        return failureHistogram.get(5);
    }

    public int getFail6() {
        return failureHistogram.get(6);
    }

    public int getFail7() {
        return failureHistogram.get(7);
    }

    @JsonIgnore
    public List<Integer> getTotalHistogram() {
        return totalHistogram;
    }

    @JsonIgnore
    public List<Integer> getFailureHistogram() {
        return failureHistogram;
    }

    @Override
    public String toString() {
        return "UriStat{" +
                "serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", timestamp=" + timestamp +
                ", uri=" + uri +
                ", apdexRaw= " + apdexRaw +
                ", count=" + count +
                ", failCount=" + failCount +
                ", maxLatencyMs=" + maxLatencyMs +
                ", totalTimeMs=" + totalTimeMs +
                ", totalHistogram=" + totalHistogram +
                ", failureHistogram=" + failureHistogram +
                '}';
    }
}
