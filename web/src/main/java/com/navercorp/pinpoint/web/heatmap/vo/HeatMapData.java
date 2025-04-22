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

package com.navercorp.pinpoint.web.heatmap.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author minwoo-jung
 */
public class HeatMapData {

    private final HeatmapSize heatmapSize;
    private final HeatmapSummary heatmapSummary;

    private final TreeMap<Long, HeatMapMetricColumn> heatMapMetricColumnMap;

    public HeatMapData(int width, int height, long totalSuccessCount, long totalFailCount, TreeMap<Long, HeatMapMetricColumn> heatMapMetricColumnMap) {
        this.heatmapSize = new HeatmapSize(width, height);
        this.heatmapSummary = new HeatmapSummary(totalSuccessCount, totalFailCount);
        this.heatMapMetricColumnMap = Objects.requireNonNull(heatMapMetricColumnMap,"heatMapMetricColumnMap");
    }

    public Map<Long, HeatMapMetricColumn> getHeatMapMetricColumnMap() {
        return heatMapMetricColumnMap;
    }

    public HeatMapMetricColumn getHeatMapMetricColumn(long timestamp) {
        return heatMapMetricColumnMap.get(timestamp);
    }

    public HeatmapSize getHeatmapSize() {
        return heatmapSize;
    }

    public HeatmapSummary getHeatmapSummary() {
        return heatmapSummary;
    }

    public List<HeatMapMetricColumn> getDescHeatMapMetricColumnList() {
        return new ArrayList<>(heatMapMetricColumnMap.descendingMap().values());
    }

    public List<HeatMapMetricColumn> getAscHeatMapMetricColumnList() {
        return new ArrayList<>(heatMapMetricColumnMap.values());
    }

    public String prettyToString() {
        String tab = "\t";
        StringBuilder sb = new StringBuilder();
        sb.append("HeatMapData \n" +
                    "{\n" +
                    tab + "size : " + heatmapSize.prettyToString(tab + "\t") + ",\n");

        for (HeatMapMetricColumn heatMapMetricColumn : heatMapMetricColumnMap.values()) {
            sb.append("\tHeatMapMetricColumn : \n").append(heatMapMetricColumn.prettyToString(tab + "\t\t\t\t\t")).append("\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
