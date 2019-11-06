/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;

import java.util.*;

/**
 * @author emeroad
 */
public class ResponseTime {
    // rowKey
    private final String applicationName;
    private final ServiceType applicationServiceType;
    private final long timeStamp;

    // agentId is the key
    private final Map<String, TimeHistogram> responseHistogramMap = new HashMap<>();


    public ResponseTime(String applicationName, ServiceType applicationServiceType, long timeStamp) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.applicationServiceType = Objects.requireNonNull(applicationServiceType, "applicationServiceType");
        this.timeStamp = timeStamp;
    }


    public String getApplicationName() {
        return applicationName;
    }

    public short getApplicationServiceType() {
        return applicationServiceType.getCode();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Histogram findHistogram(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId");
        }
        return responseHistogramMap.get(agentId);
    }

    private Histogram getHistogram(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId");
        }
        TimeHistogram histogram = responseHistogramMap.computeIfAbsent(agentId, k -> new TimeHistogram(applicationServiceType, timeStamp));
        return histogram;
    }

    public void addResponseTime(String agentId, short slotNumber, long count) {
        Histogram histogram = getHistogram(agentId);
        histogram.addCallCount(slotNumber, count);
    }


    public void addResponseTime(String agentId, Histogram copyHistogram) {
        if (copyHistogram == null) {
            throw new NullPointerException("copyHistogram");
        }
        Histogram histogram = getHistogram(agentId);
        histogram.add(copyHistogram);
    }

    public void addResponseTime(String agentId, int elapsedTime, boolean error) {
        Histogram histogram = getHistogram(agentId);
        histogram.addCallCountByElapsedTime(elapsedTime, error);
    }

    public Collection<TimeHistogram> getAgentResponseHistogramList() {
        return responseHistogramMap.values();
    }

    public Histogram getApplicationResponseHistogram() {
        Histogram result = new Histogram(applicationServiceType);
        for (Histogram histogram : responseHistogramMap.values()) {
            result.add(histogram);
        }
        return result;
    }

    public Set<Map.Entry<String, TimeHistogram>> getAgentHistogram() {
        return this.responseHistogramMap.entrySet();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseTime{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", applicationServiceType=").append(applicationServiceType);
        sb.append(", timeStamp=").append(timeStamp);
        sb.append(", responseHistogramMap=").append(responseHistogramMap);
        sb.append('}');
        return sb.toString();
    }
}
