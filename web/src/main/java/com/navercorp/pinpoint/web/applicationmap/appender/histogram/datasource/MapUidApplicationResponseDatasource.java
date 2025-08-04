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

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.uid.ApplicationUidResponse;
import com.navercorp.pinpoint.web.applicationmap.uid.hbase.MapSelfUidDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class MapUidApplicationResponseDatasource implements WasNodeHistogramDataSource {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final BaseApplicationUidService baseApplicationUidService;
    private final MapSelfUidDao selfUidDao;

    public MapUidApplicationResponseDatasource(BaseApplicationUidService baseApplicationUidService, MapSelfUidDao selfUidDao) {
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
        this.selfUidDao = Objects.requireNonNull(selfUidDao, "selfUidDao");
    }


    @Override
    public NodeHistogram createNodeHistogram(Application application, TimeWindow timeWindow) {
        ApplicationUid applicationUid = baseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode());
        ApplicationUidResponse applicationUidResponse = selfUidDao.selectApplicationResponse(ServiceUid.DEFAULT, applicationUid, application.getServiceType(), timeWindow);

        Range windowRange = timeWindow.getWindowRange();
        List<TimeHistogram> histograms = applicationUidResponse.getHistograms();
        ApplicationTimeHistogram applicationTimeHistogram = buildApplicationTimeHistogram(application, timeWindow, histograms);
        NodeHistogram.Builder builder = NodeHistogram.newBuilder(application, windowRange);
        builder.setApplicationTimeHistogram(applicationTimeHistogram);

        return builder.build();
    }

    private ApplicationTimeHistogram buildApplicationTimeHistogram(Application application, TimeWindow timeWindow, List<TimeHistogram> histogram) {
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(application, timeWindow);
        return builder.buildFromTimeHistogram(histogram);
    }
}
