/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class ResponseHistograms {

    private final Map<Application, List<ResponseTime>> responseTimeMap;

    private ResponseHistograms(Map<Application, List<ResponseTime>> responseTimeMap) {
        this.responseTimeMap = responseTimeMap;
    }

    public List<ResponseTime> getResponseTimeList(Application application) {
        List<ResponseTime> responseTimes = responseTimeMap.getOrDefault(application, Collections.emptyList());
        return Collections.unmodifiableList(responseTimes);
    }

    public static class Builder {

        private final TimeWindow window;
        private final Map<Long, Map<Application, ResponseTime>> responseTimeApplicationMap = new HashMap<>();

        public Builder(Range range) {
            if (range == null) {
                throw new NullPointerException("range");
            }
            // don't sample for now
            this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        }

        @VisibleForTesting
        TimeWindow getWindow() {
            return window;
        }

        public Builder addHistogram(Application application, SpanBo span, long timestamp) {
            timestamp = window.refineTimestamp(timestamp);

            final ResponseTime responseTime = getResponseTime(application, timestamp);
            boolean error = false;
            if (span.getErrCode() != 0) {
                error = true;
            }
            responseTime.addResponseTime(span.getAgentId(), span.getElapsed(), error);
            return this;
        }

        public void addLinkHistogram(Application application, String agentId, TimeHistogram timeHistogram) {
            long timeStamp = timeHistogram.getTimeStamp();
            timeStamp = window.refineTimestamp(timeStamp);
            final ResponseTime responseTime = getResponseTime(application, timeStamp);
            responseTime.addResponseTime(agentId, timeHistogram);
        }

        private ResponseTime getResponseTime(Application application, Long timestamp) {
            Map<Application, ResponseTime> responseTimeMap = responseTimeApplicationMap.computeIfAbsent(timestamp, (Long k) -> new HashMap<>());
            ResponseTime responseTime = responseTimeMap.get(application);
            if (responseTime == null) {
                responseTime = new ResponseTime(application.getName(), application.getServiceType(), timestamp);
                responseTimeMap.put(application, responseTime);
            }
            return responseTime;
        }

        public ResponseHistograms build() {
            final Map<Application, List<ResponseTime>> responseTimeMap = new HashMap<>();

            for (Map<Application, ResponseTime> entry : responseTimeApplicationMap.values()) {
                for (Map.Entry<Application, ResponseTime> applicationResponseTimeEntry : entry.entrySet()) {
                    List<ResponseTime> responseTimeList = responseTimeMap.computeIfAbsent(applicationResponseTimeEntry.getKey(), (Application k) -> new ArrayList<>());
                    responseTimeList.add(applicationResponseTimeEntry.getValue());
                }
            }
            return new ResponseHistograms(responseTimeMap);
        }
    }
}
