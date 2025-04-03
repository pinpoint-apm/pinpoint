/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.heatmap.service;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
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

    private final String POSTFIX_SORT_KEY_SUCCESS = "#suc";
    private final String POSTFIX_SORT_KEY_FAIL = "#fal";

    private final int YAXIS_CELL_MAXCOUNT = 50;
    private final int MIN_INTERVAL_FOR_ELAPSED_TIME = 200;

    private final HeatmapChartDao heatmapChartDao;

    public HeatmapChartServiceImpl(HeatmapChartDao heatmapChartDao) {
        this.heatmapChartDao = Objects.requireNonNull(heatmapChartDao,"heatmapChartDao");
    }

    @Override
    public HeatMapData getHeatmapAppData(String applicationName, TimeWindow timeWindow, int minYAxis, int maxYAxis) {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = createElapsedTimeBucketInfo(minYAxis, maxYAxis);
        HeatmapSearchKey heatmapSearchKey = new HeatmapSearchKey(applicationName + POSTFIX_SORT_KEY_SUCCESS,
                                                                 timeWindow,
                                                                 elapsedTimeBucketInfo.getTimeInterval(),
                                                                 elapsedTimeBucketInfo.getMin(),
                                                                 elapsedTimeBucketInfo.getMax(),
                                                                 elapsedTimeBucketInfo.findLargestMultipleBelow(),
                                                                 elapsedTimeBucketInfo.getBucketList().size());

        // TODO : (minwoo) Change to parallel execution
        long startTime = System.currentTimeMillis();
        List<HeatmapCell> successHeatmapAppData = heatmapChartDao.getHeatmapAppData(heatmapSearchKey);
        // TODO : (minwoo) remove log for performance
        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("==== successHeatmapAppData execution time: {}ms", executionTime);

        logger.debug("heatmapCell size: {}", successHeatmapAppData.size());
        for (HeatmapCell heatmapCell : successHeatmapAppData) {
            logger.debug("heatmapCell: {}", heatmapCell);
        }

        heatmapSearchKey = new HeatmapSearchKey(applicationName + POSTFIX_SORT_KEY_FAIL,
                                                timeWindow,
                                                elapsedTimeBucketInfo.getTimeInterval(),
                                                elapsedTimeBucketInfo.getMin(),
                                                elapsedTimeBucketInfo.getMax(),
                                                elapsedTimeBucketInfo.findLargestMultipleBelow(),
                                                elapsedTimeBucketInfo.getBucketList().size());

        startTime = System.currentTimeMillis();
        List<HeatmapCell> failHeatmapAppData = heatmapChartDao.getHeatmapAppData(heatmapSearchKey);
        executionTime = System.currentTimeMillis() - startTime;
        logger.debug("==== failHeatmapAppData execution time: {}ms", executionTime);

        logger.debug("heatmapCell size: {}", failHeatmapAppData.size());
        for (HeatmapCell heatmapCell : failHeatmapAppData) {
            logger.debug("heatmapCell: {}", heatmapCell);
        }

        return createHeatmapData(timeWindow, successHeatmapAppData, failHeatmapAppData, elapsedTimeBucketInfo);
    }

    private HeatMapData createHeatmapData(TimeWindow timeWindow, List<HeatmapCell> successHeatmapAppData, List<HeatmapCell> failHeatmapAppData, ElapsedTimeBucketInfo elapsedTimeBucketInfo) {
        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(successHeatmapAppData, failHeatmapAppData, timeWindow, elapsedTimeBucketInfo.getBucketList());
        return timeSeriesBuilder.createHeatMapData();
    }

    protected ElapsedTimeBucketInfo createElapsedTimeBucketInfo(int min, int max) {
        int timeInterval = calculateTimeInterval(min, max);

        List<Integer> bucketList = new ArrayList<Integer>();

        int value;
        if (min == 0) {
            value = timeInterval;
        } else {
            value = min;
        }

        while (value < max) {
            bucketList.add(value);
            value += timeInterval;
        }

        bucketList.add(max);

        return new ElapsedTimeBucketInfo(bucketList, timeInterval);
    }

    protected int calculateTimeInterval(int min, int max) {
        int range = max - min;
        if (range <= (YAXIS_CELL_MAXCOUNT * MIN_INTERVAL_FOR_ELAPSED_TIME)) {
            return MIN_INTERVAL_FOR_ELAPSED_TIME;
        }

        if (min == 0) {
            return range / (YAXIS_CELL_MAXCOUNT);
        } else {
            return range / (YAXIS_CELL_MAXCOUNT - 1);
        }
    }
}
