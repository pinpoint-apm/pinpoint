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

package com.navercorp.pinpoint.web.applicationmap.dao;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 */
public class ApplicationResponse {
    // rowKey
    private final Application application;
    // agentId is the key
    private final List<TimeHistogram> histograms;
    private final Set<String> agentIdMap;


    ApplicationResponse(Application application,
                        List<TimeHistogram> histograms,
                        Set<String> agentIdMap) {
        this.application = Objects.requireNonNull(application, "application");
        this.histograms = Objects.requireNonNull(histograms, "histograms");
        this.agentIdMap = Objects.requireNonNull(agentIdMap, "agentIdMap");
    }

    public Application getApplication() {
        return application;
    }

    @Deprecated
    public Set<String> getAgentIds() {
        return agentIdMap;
    }


    public List<TimeHistogram> getApplicationHistograms() {
        return histograms;
    }

    public Histogram getApplicationTotalHistogram() {
        return Histogram.sumOf(application.getServiceType(), histograms);
    }

    @Override
    public String toString() {
        return "ApplicationHistogram{" +
                "application='" + application + '\'' +
                ", histograms=" + histograms +
                ", agentIdMap=" + agentIdMap +
                '}';
    }

    public static Builder newBuilder(Application application) {
        return new Builder(application);
    }

    public static class Builder {

        private final Map<Long, TimeHistogram> histogramMap;
        // agentId is the key
        private final Set<String> agentIdMap;

        private final Application application;

        Builder(Application application) {
            this.application = Objects.requireNonNull(application, "application");
            this.histogramMap = new HashMap<>();
            this.agentIdMap = new HashSet<>();
        }

        public Application getApplication() {
            return application;
        }

        public String getApplicationName() {
            return application.getName();
        }

        public ServiceType getApplicationServiceType() {
            return application.getServiceType();
        }

        public void addResponseTime(String agentId, long timeStamp, short slotNumber, long count) {
            this.agentIdMap.add(agentId);
            TimeHistogram timeHistogram = getTimeHistogram(timeStamp);
            timeHistogram.addCallCount(slotNumber, count);
        }

        public void addResponseTimeBySlotCode(String agentId, long timeStamp, byte code, long count) {
            this.agentIdMap.add(agentId);
            TimeHistogram timeHistogram = getTimeHistogram(timeStamp);
            timeHistogram.addCallCountByCode(code, count);
        }

        private TimeHistogram getTimeHistogram(long timeStamp) {
            return this.histogramMap.computeIfAbsent(timeStamp, k -> new TimeHistogram(application.getServiceType(), timeStamp));
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

        public ApplicationResponse build() {
            List<TimeHistogram> list = new ArrayList<>(this.histogramMap.values());
            list.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
            return new ApplicationResponse(application, list, this.agentIdMap);
        }
    }
}
