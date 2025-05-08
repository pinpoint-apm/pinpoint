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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 */
public class MapApplicationResponseNodeHistogramDataSource implements WasNodeHistogramDataSource {

    private final MapResponseDao mapResponseDao;

    public MapApplicationResponseNodeHistogramDataSource(MapResponseDao mapResponseDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    @Override
    public NodeHistogram createNodeHistogram(Application application, TimeWindow timeWindow) {
        ApplicationHistogram applicationHistogram = mapResponseDao.selectApplicationResponseTime(application, timeWindow);

        List<TimeHistogram> histogram = applicationHistogram.getApplicationHistograms();
        ApplicationTimeHistogram applicationTimeHistogram = new ApplicationTimeHistogram(application, histogram);

        Range windowRange = timeWindow.getWindowRange();
        NodeHistogram.Builder builder = NodeHistogram.newBuilder(application, windowRange);
        builder.setApplicationTimeHistogram(applicationTimeHistogram);

        Histogram appHistogram = getHistogram(application, applicationTimeHistogram);
        builder.setApplicationHistogram(appHistogram);

        Map<String, Histogram> agentMap = getAgentIdMap(application, applicationHistogram.getAgentIds());
        builder.setAgentHistogramMap(agentMap);
        return builder.build();
    }

    private Histogram getHistogram(Application application, ApplicationTimeHistogram applicationTimeHistogram) {
        Histogram histogram = new Histogram(application.getServiceType());
        applicationTimeHistogram.getHistogramList().forEach(histogram::add);
        return histogram;
    }

    private Map<String, Histogram> getAgentIdMap(Application application, Set<String> agentIds) {
        Map<String, Histogram> agentMap = new HashMap<>();
        Histogram emptyHistogram = new Histogram(application.getServiceType());
        for (String agentId : agentIds) {
            agentMap.put(agentId, emptyHistogram);
        }
        return agentMap;
    }
}
