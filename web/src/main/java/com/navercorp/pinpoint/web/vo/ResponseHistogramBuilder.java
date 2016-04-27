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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ResponseHistogramBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeWindow window;

    private Map<Long, Map<Application, ResponseTime>> responseTimeApplicationMap = new HashMap<>();
    private Map<Application, List<ResponseTime>> result = new HashMap<>();


    public ResponseHistogramBuilder(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        // don't sample for now
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);

    }

    public void addHistogram(Application application, SpanBo span, long timeStamp) {
        timeStamp = window.refineTimestamp(timeStamp);


        final ResponseTime responseTime = getResponseTime(application, timeStamp);
        boolean error = false;
        if (span.getErrCode() != 0) {
            error = true;
        }
        responseTime.addResponseTime(span.getAgentId(), span.getElapsed(), error);
    }


    public void addLinkHistogram(Application application, String agentId, TimeHistogram timeHistogram) {
        long timeStamp = timeHistogram.getTimeStamp();
        timeStamp = window.refineTimestamp(timeStamp);
        final ResponseTime responseTime = getResponseTime(application, timeStamp);
        responseTime.addResponseTime(agentId, timeHistogram);
    }

    private ResponseTime getResponseTime(Application application, Long timeStamp) {
        Map<Application, ResponseTime> responseTimeMap = responseTimeApplicationMap.get(timeStamp);
        if (responseTimeMap == null) {
            responseTimeMap = new HashMap<>();
            responseTimeApplicationMap.put(timeStamp, responseTimeMap);
        }
        ResponseTime responseTime = responseTimeMap.get(application);
        if (responseTime == null) {
            responseTime = new ResponseTime(application.getName(), application.getServiceType(), timeStamp);
            responseTimeMap.put(application, responseTime);
        }
        return responseTime;
    }

    public void build() {
        final Map<Application, List<ResponseTime>> result = new HashMap<>();

        for (Map<Application, ResponseTime> entry : responseTimeApplicationMap.values()) {
            for (Map.Entry<Application, ResponseTime> applicationResponseTimeEntry : entry.entrySet()) {
                List<ResponseTime> responseTimeList = result.get(applicationResponseTimeEntry.getKey());
                if (responseTimeList == null) {
                    responseTimeList = new ArrayList<>();
                    Application key = applicationResponseTimeEntry.getKey();
                    result.put(key, responseTimeList);
                }
                responseTimeList.add(applicationResponseTimeEntry.getValue());
            }
        }

        this.responseTimeApplicationMap = null;
        this.result = result;

    }

    public List<ResponseTime> getResponseTimeList(Application application) {
        List<ResponseTime> responseTimes = this.result.get(application);
        return responseTimes;
    }


}
