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

package com.navercorp.pinpoint.web.heatmap.service;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.heatmap.dao.HeatmapChartDao;
import com.navercorp.pinpoint.web.heatmap.util.TimeSeriesBuilder;
import com.navercorp.pinpoint.web.heatmap.vo.ElapsedTimeBucketInfo;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapCell;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapSearchKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Service
public class HeatmapChartServiceImpl implements HeatmapChartService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String POSTFIX_SORT_KEY_SUCCESS = "#suc";
    private static final String POSTFIX_SORT_KEY_FAIL = "#fal";

    private static final int YAXIS_CELL_MAXCOUNT = 50;
    private static final int MIN_INTERVAL_FOR_ELAPSED_TIME = 200;

    private final HeatmapChartDao heatmapChartDao;

    public HeatmapChartServiceImpl(HeatmapChartDao heatmapChartDao) {
        this.heatmapChartDao = Objects.requireNonNull(heatmapChartDao,"heatmapChartDao");
    }

    @Override
    public HeatMapData getHeatmapAppData(String serviceName, String applicationName, TimeWindow timeWindow, int minElapsedTime, int maxElapsedTime) {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = createElapsedTimeBucketInfo(minElapsedTime, maxElapsedTime);
        String sortKeyPrefix = serviceName + "#" +applicationName;
        HeatmapSearchKey heatmapSearchKey = new HeatmapSearchKey(sortKeyPrefix + POSTFIX_SORT_KEY_SUCCESS,
                                                                 timeWindow,
                                                                 elapsedTimeBucketInfo.timeInterval(),
                                                                 elapsedTimeBucketInfo.min(),
                                                                 elapsedTimeBucketInfo.max(),
                                                                 elapsedTimeBucketInfo.findLargestMultipleBelow(),
                                                                 elapsedTimeBucketInfo.bucketList().size());
        List<HeatmapCell> successHeatmapAppData = heatmapChartDao.getHeatmapAppData(heatmapSearchKey);

        heatmapSearchKey = new HeatmapSearchKey(sortKeyPrefix + POSTFIX_SORT_KEY_FAIL,
                                                timeWindow,
                                                elapsedTimeBucketInfo.timeInterval(),
                                                elapsedTimeBucketInfo.min(),
                                                elapsedTimeBucketInfo.max(),
                                                elapsedTimeBucketInfo.findLargestMultipleBelow(),
                                                elapsedTimeBucketInfo.bucketList().size());
        List<HeatmapCell> failHeatmapAppData = heatmapChartDao.getHeatmapAppData(heatmapSearchKey);

        return createHeatmapData(timeWindow, successHeatmapAppData, failHeatmapAppData, elapsedTimeBucketInfo);
    }

    private HeatMapData createHeatmapData(TimeWindow timeWindow, List<HeatmapCell> successHeatmapAppData, List<HeatmapCell> failHeatmapAppData, ElapsedTimeBucketInfo elapsedTimeBucketInfo) {
        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(successHeatmapAppData, failHeatmapAppData, timeWindow, elapsedTimeBucketInfo.bucketList());
        return timeSeriesBuilder.createHeatMapData();
    }

    protected ElapsedTimeBucketInfo createElapsedTimeBucketInfo(int minElapsedTime, int maxElapsedTime) {
        int timeInterval = calculateTimeInterval(minElapsedTime, maxElapsedTime);

        List<Integer> bucketList = new ArrayList<Integer>();

        int value;
        if (minElapsedTime == 0) {
            value = timeInterval;
        } else {
            value = minElapsedTime;
        }

        while (value < maxElapsedTime) {
            bucketList.add(value);
            value += timeInterval;
        }

        bucketList.add(maxElapsedTime);

        return new ElapsedTimeBucketInfo(bucketList, timeInterval);
    }

    protected int calculateTimeInterval(int minElapsedTime, int maxElapsedTime) {
        int range = maxElapsedTime - minElapsedTime;
        if (range <= (YAXIS_CELL_MAXCOUNT * MIN_INTERVAL_FOR_ELAPSED_TIME)) {
            return MIN_INTERVAL_FOR_ELAPSED_TIME;
        }

        if (minElapsedTime == 0) {
            return range / (YAXIS_CELL_MAXCOUNT);
        } else {
            return range / (YAXIS_CELL_MAXCOUNT - 1);
        }
    }
}
