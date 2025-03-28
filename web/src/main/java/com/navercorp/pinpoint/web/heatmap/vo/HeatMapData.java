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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author minwoo-jung
 */
public class HeatMapData {

    private final HeatmapSize heatmapSize;

    private final Map<Long, HeatMapMetricColumn> heatMapMetricColumnMap;

    public HeatMapData(int width, int height, Map<Long, HeatMapMetricColumn> heatMapMetricColumnMap) {
        this.heatmapSize = new HeatmapSize(width, height);
        this.heatMapMetricColumnMap = Objects.requireNonNull(heatMapMetricColumnMap,"heatMapMetricColumnMap");
    }

    public Map<Long, HeatMapMetricColumn> getHeatMapMetricColumnMap() {
        return heatMapMetricColumnMap;
    }

    public HeatmapSize getHeatmapSize() {
        return heatmapSize;
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
