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
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        private final Map<Long, Map<Application, ResponseTime.Builder>> map = new HashMap<>();

        public Builder(TimeWindow window) {
            // don't sample for now
            this.window = Objects.requireNonNull(window, "window");
        }

        @VisibleForTesting
        TimeWindow getWindow() {
            return window;
        }

        public Builder addHistogram(Application application, SpanBo span, long timestamp) {
            timestamp = window.refineTimestamp(timestamp);

            final ResponseTime.Builder responseTime = getBuilder(application, timestamp);
            final boolean error = isError(span);
            responseTime.addResponseTime(span.getAgentId(), span.getElapsed(), error);
            return this;
        }

        private boolean isError(SpanBo span) {
            if (span.getErrCode() != 0) {
                return true;
            }
            return false;
        }

        public void addLinkHistogram(Application application, String agentId, TimeHistogram timeHistogram) {
            long timeStamp = timeHistogram.getTimeStamp();
            timeStamp = window.refineTimestamp(timeStamp);
            final ResponseTime.Builder responseTime = getBuilder(application, timeStamp);
            responseTime.addResponseTime(agentId, timeHistogram);
        }

        private ResponseTime.Builder getBuilder(Application application, long timestamp) {
            Map<Application, ResponseTime.Builder> responseTimeMap = map.computeIfAbsent(timestamp, (Long k) -> new HashMap<>());
            ResponseTime.Builder responseTime = responseTimeMap.get(application);
            if (responseTime == null) {
                responseTime = ResponseTime.newBuilder(application.getName(), application.getServiceType(), timestamp);
                responseTimeMap.put(application, responseTime);
            }
            return responseTime;
        }

        public ResponseHistograms build() {
            final Map<Long, Map<Application, ResponseTime.Builder>> copyMap = this.map;

            final Map<Application, List<ResponseTime>> responseTimeMap = new HashMap<>(copyMap.size());

            for (Map<Application, ResponseTime.Builder> entry : copyMap.values()) {
                for (Map.Entry<Application, ResponseTime.Builder> applicationResponseTimeEntry : entry.entrySet()) {
                    Application application = applicationResponseTimeEntry.getKey();
                    ResponseTime.Builder builder = applicationResponseTimeEntry.getValue();

                    List<ResponseTime> responseTimeList = responseTimeMap.computeIfAbsent(application, (Application k) -> new ArrayList<>());
                    responseTimeList.add(builder.build());
                }
            }
            return new ResponseHistograms(responseTimeMap);
        }
    }
}
