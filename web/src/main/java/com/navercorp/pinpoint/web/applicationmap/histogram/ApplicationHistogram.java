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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 */
public class ApplicationHistogram {
    // rowKey
    private final String applicationName;
    private final ServiceType applicationServiceType;
    // agentId is the key
    private final List<TimeHistogram> histograms;
    private final Set<String> agentIdMap;


    ApplicationHistogram(String applicationName,
                         ServiceType applicationServiceType,
                         List<TimeHistogram> histograms,
                         Set<String> agentIdMap) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.applicationServiceType = Objects.requireNonNull(applicationServiceType, "applicationServiceType");
        this.histograms = Objects.requireNonNull(histograms, "histograms");
        this.agentIdMap = Objects.requireNonNull(agentIdMap, "agentIdMap");
    }


    public String getApplicationName() {
        return applicationName;
    }

    public int getApplicationServiceType() {
        return applicationServiceType.getCode();
    }

    public Set<String> getAgentIds() {
        return agentIdMap;
    }


    public List<TimeHistogram> getApplicationHistograms() {
        return histograms;
    }

    @Override
    public String toString() {
        return "ApplicationHistogram{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", histograms=" + histograms +
                ", agentIdMap=" + agentIdMap +
                '}';
    }

    public static Builder newBuilder(String applicationName, ServiceType applicationServiceType) {
        return new Builder(applicationName, applicationServiceType);
    }

    public static class Builder {

        private final Map<Long, TimeHistogram> histogramMap;
        // agentId is the key
        private final Set<String> agentIdMap;

        private final String applicationName;
        private final ServiceType applicationServiceType;

        Builder(String applicationName, ServiceType applicationServiceType) {
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.applicationServiceType = Objects.requireNonNull(applicationServiceType, "applicationServiceType");
            this.histogramMap = new HashMap<>();
            this.agentIdMap = new HashSet<>();
        }

        public String getApplicationName() {
            return applicationName;
        }

        public ServiceType getApplicationServiceType() {
            return applicationServiceType;
        }

        public void addResponseTime(String agentId, long timeStamp, short slotNumber, long count) {
            this.agentIdMap.add(agentId);
            TimeHistogram timeHistogram = getTimeHistogram(timeStamp);
            timeHistogram.addCallCount(slotNumber, count);
        }

        private TimeHistogram getTimeHistogram(long timeStamp) {
            return this.histogramMap.computeIfAbsent(timeStamp, k -> new TimeHistogram(applicationServiceType, timeStamp));
        }


        public void addResponseTime(String agentId, long timestamp, Histogram copyHistogram) {
            Objects.requireNonNull(agentId, "agentId");
            Objects.requireNonNull(copyHistogram, "copyHistogram");

            this.agentIdMap.add(agentId);
            TimeHistogram histogram = getTimeHistogram(timestamp);
            histogram.add(copyHistogram);
        }

        public void addResponseTime(String agentId, long timestamp, int elapsedTime, boolean error) {
            Objects.requireNonNull(agentId, "agentId");

            this.agentIdMap.add(agentId);

            TimeHistogram histogram = getTimeHistogram(timestamp);
            histogram.addCallCountByElapsedTime(elapsedTime, error);
        }

        public ApplicationHistogram build() {
            List<TimeHistogram> list = new ArrayList<>(this.histogramMap.values());
            list.sort(Comparator.comparing(TimeHistogram::getTimeStamp));
            return new ApplicationHistogram(applicationName, applicationServiceType, list, this.agentIdMap);
        }
    }
}
