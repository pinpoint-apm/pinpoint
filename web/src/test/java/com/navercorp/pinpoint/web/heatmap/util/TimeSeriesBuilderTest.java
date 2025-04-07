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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricCell;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricColumn;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapCell;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
            assertEquals(bucketList.size(), heatMapMetricColumn.heatMapMetricCellMap().size());
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
            assertEquals(bucketList.size(), heatMapMetricColumn.heatMapMetricCellMap().size());
        }
    }

    @Test
    public void sizeTest() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        List<HeatmapCell> heatmapCellList = Collections.emptyList();
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(heatmapCellList, heatmapCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();
        assertEquals(31, heatMapData.getHeatMapMetricColumnMap().size());
        assertEquals(31, heatMapData.getHeatmapSize().width());
        assertEquals(50, heatMapData.getHeatmapSize().height());
    }

    @Test
    public void timestampTest() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        List<HeatmapCell> heatmapCellList = Collections.emptyList();
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(heatmapCellList, heatmapCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();

        List<HeatMapMetricColumn> heatMapMetricColumnList = heatMapData.getDescHeatMapMetricColumnList();
        int size = heatMapMetricColumnList.size();
        assertEquals(31, size);

        long timestamp = heatMapMetricColumnList.get(0).timestamp();
        assertEquals(1742826240000L, timestamp);

        timestamp = heatMapMetricColumnList.get(size - 1).timestamp();
        assertEquals(1742567040000L, timestamp);
    }

    @Test
    public void successAndFailCountTest() {
        long from = 1742569200000L; // 2025-03-22 00:00:00
        long to = 1742828399000L; // 2025-03-24 23:59:59
        Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler(10000L, 30L));
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);


        long timestamp1 = 1742826240000L;
        long timestamp2 = 1742567040000L;
        long timestamp3 = 1742662080000L;
        int elapsedTime1 = 200;
        int elapsedTime2 = 800;
        int elapsedTime3 = 2200;
        int successCount1 = 1234;
        int successCount2 = 12340;
        int successCount3 = 1234012;
        int failCount1 = 123488;
        int failCount2 = 12120;
        int failCount3 = 90999;

        List<HeatmapCell> successCellList = new ArrayList<>(3);
        successCellList.add(new HeatmapCell(timestamp1, elapsedTime1, successCount1));
        successCellList.add(new HeatmapCell(timestamp2, elapsedTime2, successCount2));
        successCellList.add(new HeatmapCell(timestamp3, elapsedTime3, successCount3));

        List<HeatmapCell> failCellList = new ArrayList<>(3);
        failCellList.add(new HeatmapCell(timestamp1, elapsedTime1, failCount1));
        failCellList.add(new HeatmapCell(timestamp2, elapsedTime2, failCount2));
        failCellList.add(new HeatmapCell(timestamp3, elapsedTime3, failCount3));

        TimeSeriesBuilder timeSeriesBuilder = new TimeSeriesBuilder(successCellList, failCellList, timeWindow, bucketList);
        HeatMapData heatMapData = timeSeriesBuilder.createHeatMapData();

        HeatMapMetricColumn heatMapMetricColumn = heatMapData.getHeatMapMetricColumn(timestamp1);
        HeatMapMetricCell heatMapMetricCell = heatMapMetricColumn.getHeatMapMetricCell(elapsedTime1);
        assertEquals(successCount1, heatMapMetricCell.getSuccessCount());
        assertEquals(failCount1, heatMapMetricCell.getFailCount());

        heatMapMetricColumn = heatMapData.getHeatMapMetricColumn(timestamp2);
        heatMapMetricCell = heatMapMetricColumn.getHeatMapMetricCell(elapsedTime2);
        assertEquals(successCount2, heatMapMetricCell.getSuccessCount());
        assertEquals(failCount2, heatMapMetricCell.getFailCount());

        heatMapMetricColumn = heatMapData.getHeatMapMetricColumn(timestamp3);
        heatMapMetricCell = heatMapMetricColumn.getHeatMapMetricCell(elapsedTime3);
        assertEquals(successCount3, heatMapMetricCell.getSuccessCount());
        assertEquals(failCount3, heatMapMetricCell.getFailCount());
    }
}