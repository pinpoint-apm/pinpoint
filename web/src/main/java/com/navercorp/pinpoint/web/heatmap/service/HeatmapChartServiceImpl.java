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
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapCell;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapSearchKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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

    private final HeatmapChartDao heatmapChartDao;
    private final int yAxisCellMaxCount;
    private final int minIntervalForElapsedTime;

    public HeatmapChartServiceImpl(HeatmapChartDao heatmapChartDao) {
        this.heatmapChartDao = Objects.requireNonNull(heatmapChartDao,"heatmapChartDao");
        this.yAxisCellMaxCount = 50;
        this.minIntervalForElapsedTime = 200;
    }

    @Override
    public void getHeatmapAppData(String applicationName, TimeWindow timeWindow, int minYAxis, int maxYAxis) {
        int elapsedTimeInterval = calculateTimeInterval(minYAxis, maxYAxis);
        HeatmapSearchKey heatmapSearchKey = new HeatmapSearchKey(applicationName + POSTFIX_SORT_KEY_SUCCESS, timeWindow, elapsedTimeInterval, minYAxis, maxYAxis, yAxisCellMaxCount);
        List<HeatmapCell> heatmapAppData = heatmapChartDao.getHeatmapAppData(heatmapSearchKey);
    }


    public int calculateTimeInterval(int min, int max) {
        int range = max - min;
        if (range <= (yAxisCellMaxCount * minIntervalForElapsedTime)) {
            return minIntervalForElapsedTime;
        }
        int interval = (max - min) / (yAxisCellMaxCount);

        return interval;
    }
}
