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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricColumn;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapCell;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo-jung
 */
class TimeSeriesBuilderTest {

    @Test
    public void initHeatmapMetricCellTest() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        List<HeatmapCell> heatmapCellList = Collections.emptyList();
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(heatmapCellList, heatmapCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();
        assertEquals(31, heatMapData.getHeatMapMetricColumnMap().size());

        for (HeatMapMetricColumn heatMapMetricColumn : heatMapData.getHeatMapMetricColumnMap().values()) {
            assertEquals(bucketList.size(), heatMapMetricColumn.getHeatMapMetricCellMap().size());
        }
    }

    @Test
    public void initHeatmapMetricCellTest2() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        List<HeatmapCell> heatmapCellList = Collections.emptyList();
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000);

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(heatmapCellList, heatmapCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();
        assertEquals(31, heatMapData.getHeatMapMetricColumnMap().size());

        for (HeatMapMetricColumn heatMapMetricColumn : heatMapData.getHeatMapMetricColumnMap().values()) {
            assertEquals(bucketList.size(), heatMapMetricColumn.getHeatMapMetricCellMap().size());
        }
    }

    @Test
    public void initHeatmapMetricCellTest3() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        List<HeatmapCell> heatmapCellList = Collections.emptyList();
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(heatmapCellList, heatmapCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();
        assertEquals(31, heatMapData.getHeatMapMetricColumnMap().size());
        assertEquals(31, heatMapData.getHeatmapSize().getWidth());
        assertEquals(50, heatMapData.getHeatmapSize().getHeight());
    }

}