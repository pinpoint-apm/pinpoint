/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 */
public class ResponseTime {
    // rowKey
    private final String applicationName;
    private final ServiceType applicationServiceType;
    private final long timeStamp;

    // agentId is the key
    private final Map<String, TimeHistogram> responseHistogramMap;


    ResponseTime(String applicationName,
                        ServiceType applicationServiceType,
                        long timeStamp,
                        Map<String, TimeHistogram> responseHistogramMap) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.applicationServiceType = Objects.requireNonNull(applicationServiceType, "applicationServiceType");
        this.timeStamp = timeStamp;
        this.responseHistogramMap = Objects.requireNonNull(responseHistogramMap, "responseHistogramMap");
    }


    public String getApplicationName() {
        return applicationName;
    }

    public int getApplicationServiceType() {
        return applicationServiceType.getCode();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Histogram findHistogram(String agentId) {
        Objects.requireNonNull(agentId, "agentId");

        return responseHistogramMap.get(agentId);
    }

    public Set<String> getAgentIds() {
        return responseHistogramMap.keySet();
    }

    public Collection<TimeHistogram> getAgentResponseHistogramList() {
        return responseHistogramMap.values();
    }

    public Histogram getApplicationResponseHistogram() {
        return Histogram.sumOf(applicationServiceType, responseHistogramMap.values());
    }

    public Set<Map.Entry<String, TimeHistogram>> getAgentHistogram() {
        return this.responseHistogramMap.entrySet();
    }

    @Override
    public String toString() {
        return "ResponseTime{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", timeStamp=" + timeStamp +
                ", responseHistogramMap=" + responseHistogramMap +
                '}';
    }

    public static Builder newBuilder(String applicationName, ServiceType applicationServiceType, long timeStamp) {
        return new Builder(applicationName, applicationServiceType, timeStamp);
    }

    public static class Builder {

        // agentId is the key
        private final Map<String, TimeHistogram> responseHistogramMap = new HashMap<>();

        private final String applicationName;
        private final ServiceType applicationServiceType;
        private final long timeStamp;

        public Builder(String applicationName, ServiceType applicationServiceType, long timeStamp) {
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.applicationServiceType = Objects.requireNonNull(applicationServiceType, "applicationServiceType");
            this.timeStamp = timeStamp;
        }

        public void addResponseTime(String agentId, short slotNumber, long count) {
            Histogram histogram = getHistogram(agentId);
            histogram.addCallCount(slotNumber, count);
        }

        public void addResponseTimeByCode(String agentId, byte slotCode, long count) {
            Histogram histogram = getHistogram(agentId);
            histogram.addCallCountByCode(slotCode, count);
        }

        public void addResponseTime(String agentId, Histogram copyHistogram) {
            Objects.requireNonNull(copyHistogram, "copyHistogram");

            Histogram histogram = getHistogram(agentId);
            histogram.add(copyHistogram);
        }

        public void addResponseTime(String agentId, int elapsedTime, boolean error) {
            Histogram histogram = getHistogram(agentId);
            histogram.addCallCountByElapsedTime(elapsedTime, error);
        }

        private Histogram getHistogram(String agentId) {
            Objects.requireNonNull(agentId, "agentId");

            return responseHistogramMap.computeIfAbsent(agentId, k -> new TimeHistogram(applicationServiceType, timeStamp));
        }

        public ResponseTime build() {
            return new ResponseTime(applicationName, applicationServiceType, timeStamp, this.responseHistogramMap);
        }
    }
}
