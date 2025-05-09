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

package com.navercorp.pinpoint.web.applicationmap.dao;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.List;
import java.util.Map;

/**
 *
 * @author emeroad
 * @author netspider
 * 
 */
public interface MapResponseDao {
    List<ResponseTime> selectResponseTime(Application application, TimeWindow timeWindow);

    default ApplicationResponse selectApplicationResponse(Application application, TimeWindow timeWindow) {
        List<ResponseTime> responseTimes = selectResponseTime(application, timeWindow);
        ApplicationResponse.Builder builder = ApplicationResponse.newBuilder(application);
        for (ResponseTime responseTime : responseTimes) {
            for (Map.Entry<String, TimeHistogram> entry : responseTime.getAgentHistogram()) {
                String agentId = entry.getKey();
                TimeHistogram timeHistogram = entry.getValue();
                builder.addResponseTime(agentId, timeHistogram.getTimeStamp(), timeHistogram);
            }
        }
        return builder.build();
    }
}
