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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class MapResponseNodeHistogramDataSource implements WasNodeHistogramDataSource {

    private final MapAgentResponseDao mapAgentResponseDao;

    public MapResponseNodeHistogramDataSource(MapAgentResponseDao mapAgentResponseDao) {
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public NodeHistogram createNodeHistogram(Application application, TimeWindow timeWindow) {
        List<ResponseTime> responseTimes = mapAgentResponseDao.selectResponseTime(application, timeWindow);

        Range windowRange = timeWindow.getWindowRange();
        NodeHistogram.Builder builder = NodeHistogram.newBuilder(application, windowRange);
        builder.setResponseHistogram(responseTimes);
        return builder.build();
    }
}
