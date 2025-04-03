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

package com.navercorp.pinpoint.web.heatmap.util;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricCell;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricColumn;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapCell;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author minwoo-jung
 */
public class TimeSeriesBuilder {
    private final TimeWindow timeWindow;
    private final List<Integer> bucketList;
    private final List<HeatmapCell> successHeatmapAppData;
    private final List<HeatmapCell> failHeatmapAppData;

    public TimeSeriesBuilder(List<HeatmapCell> successHeatmapAppData, List<HeatmapCell> failHeatmapAppData, TimeWindow timeWindow, List<Integer> bucketList) {
        this.successHeatmapAppData = Objects.requireNonNull(successHeatmapAppData,"successHeatmapAppData");
        this.failHeatmapAppData = Objects.requireNonNull(failHeatmapAppData,"failHeatmapAppData");
        this.timeWindow = Objects.requireNonNull(timeWindow,"timeWindow");
        this.bucketList = Objects.requireNonNull(bucketList,"bucketList");
    }

    public HeatMapData createHeatMapData() {
        int numTimeslots = timeWindow.getWindowRangeCount();
        TreeMap<Long, HeatMapMetricColumn> heatMapMetricColumnMap = new TreeMap<>();

        int columnNumber = numTimeslots - 1;
        for (long timestamp : timeWindow) {
            Map<Integer, HeatMapMetricCell> heatMapMetricCellMap = initHeatmapMetricCellMap();
            heatMapMetricColumnMap.put(timestamp, new HeatMapMetricColumn(columnNumber, timestamp, heatMapMetricCellMap));
            columnNumber--;
        }

        successHeatmapAppData.forEach(heatmapCell -> {
            HeatMapMetricColumn heatMapMetricColumn = heatMapMetricColumnMap.get(heatmapCell.getTimestamp());
            if (heatMapMetricColumn != null) {
                HeatMapMetricCell heatMapMetricCell = heatMapMetricColumn.getHeatMapMetricCell(heatmapCell.getElapsedTime());
                if (heatMapMetricCell != null) {
                    heatMapMetricCell.updateSuccessCount(heatmapCell.getCount());
                }
            }
        });

        failHeatmapAppData.forEach(heatmapCell -> {
            HeatMapMetricColumn heatMapMetricColumn = heatMapMetricColumnMap.get(heatmapCell.getTimestamp());
            if (heatMapMetricColumn != null) {
                HeatMapMetricCell heatMapMetricCell = heatMapMetricColumn.getHeatMapMetricCell(heatmapCell.getElapsedTime());
                if (heatMapMetricCell != null) {
                    heatMapMetricCell.updateFailCount(heatmapCell.getCount());
                }
            }
        });

        return new HeatMapData(timeWindow.getWindowRangeCount(), bucketList.size(), heatMapMetricColumnMap);
    }

    protected Map<Integer, HeatMapMetricCell> initHeatmapMetricCellMap() {
        TreeMap<Integer, HeatMapMetricCell> heatMapMetricCellMap = new TreeMap<>();

        for(int i = 0; i < bucketList.size(); i++) {
            heatMapMetricCellMap.put(bucketList.get(i), new HeatMapMetricCell(i, bucketList.get(i)));
        }

        return heatMapMetricCellMap;
    }
}
