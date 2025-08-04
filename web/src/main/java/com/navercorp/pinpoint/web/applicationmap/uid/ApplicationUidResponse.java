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

package com.navercorp.pinpoint.web.applicationmap.uid;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplicationUidResponse {

    private final int serviceUid;
    private final long applicationUid;
    private final ServiceType serviceType;

    private final List<TimeHistogram> histograms;

    public ApplicationUidResponse(int serviceUid,
                                  long applicationUid,
                                  ServiceType serviceType,
                                  List<TimeHistogram> histograms) {
        this.serviceUid = serviceUid;
        this.applicationUid = applicationUid;
        this.serviceType = serviceType;
        this.histograms = histograms;
    }

    public int getServiceUid() {
        return serviceUid;
    }

    public long getApplicationUid() {
        return applicationUid;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public List<TimeHistogram> getHistograms() {
        return histograms;
    }

    @Override
    public String toString() {
        return "ApplicationUidResponse{" +
               "serviceUid=" + serviceUid +
               ", applicationUid=" + applicationUid +
               ", serviceType=" + serviceType +
               ", histograms=" + histograms +
               '}';
    }

    public static ApplicationUidResponse.Builder newBuilder(int serviceUid, long applicationUid, ServiceType serviceType) {
        return new ApplicationUidResponse.Builder(serviceUid, applicationUid, serviceType);
    }

    public static class Builder {

        private final Map<Long, TimeHistogram> histogramMap;
        private final int serviceUid;
        private final long applicationUid;
        private final ServiceType serviceType;


        Builder(int serviceUid,  long applicationUid, ServiceType serviceType) {
            this.serviceUid = serviceUid;
            this.applicationUid = applicationUid;
            this.serviceType = Objects.requireNonNull(serviceType, "serviceType");

            this.histogramMap = new HashMap<>();
        }

        public Map<Long, TimeHistogram> getHistogramMap() {
            return histogramMap;
        }

        public int getServiceUid() {
            return serviceUid;
        }

        public long getApplicationUid() {
            return applicationUid;
        }

        public ServiceType getServiceType() {
            return serviceType;
        }


        public void addResponseTime(long timeStamp, short slotNumber, long count) {
            TimeHistogram timeHistogram = getTimeHistogram(timeStamp);
            timeHistogram.addCallCount(slotNumber, count);
        }

        private TimeHistogram getTimeHistogram(long timeStamp) {
            return this.histogramMap.computeIfAbsent(timeStamp, k -> new TimeHistogram(serviceType, timeStamp));
        }


        public void addResponseTime(String agentId, long timestamp, Histogram copyHistogram) {
            Objects.requireNonNull(agentId, "agentId");
            Objects.requireNonNull(copyHistogram, "copyHistogram");

            TimeHistogram histogram = getTimeHistogram(timestamp);
            histogram.add(copyHistogram);
        }

        public void addResponseTime(long timestamp, int elapsedTime, boolean error) {
            TimeHistogram histogram = getTimeHistogram(timestamp);
            histogram.addCallCountByElapsedTime(elapsedTime, error);
        }

        public ApplicationUidResponse build() {
            List<TimeHistogram> list = new ArrayList<>(this.histogramMap.values());
            list.sort(TimeHistogram.TIME_STAMP_ASC_COMPARATOR);
            return new ApplicationUidResponse(serviceUid, applicationUid, serviceType, list);
        }
    }
}
