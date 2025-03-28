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

import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatMapMetricColumn {

    private final long timestamp;
    private final int Column;

    private final Map<Integer, HeatMapMetricCell> heatMapMetricCellMap;

    public HeatMapMetricColumn(int columnNumber, long timestamp, Map<Integer, HeatMapMetricCell> heatMapMetricCellMap) {
        this.Column = columnNumber;
        this.timestamp = timestamp;
        this.heatMapMetricCellMap = Objects.requireNonNull(heatMapMetricCellMap,"heatMapMetricCellMap");;
    }

    public Map<Integer, HeatMapMetricCell> getHeatMapMetricCellMap() {
        return heatMapMetricCellMap;
    }

    public HeatMapMetricCell getHeatMapMetricCell(int elapsedTime) {
        return heatMapMetricCellMap.get(elapsedTime);
    }

    public String prettyToString(String tab) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab + "{\n" +
                    tab + "\ttimestamp= " + timestamp + ",\n" +
                    tab + "\ttime= " + DateTimeFormatUtils.formatSimple(timestamp) + ",\n" +
                    tab + "\tColumn= " + Column + ",\n" +
                    tab + "\theatMapMetricCellList : \n");

        for (HeatMapMetricCell heatMapMetricCell : heatMapMetricCellMap.values()) {
            sb.append(heatMapMetricCell.prettyToString(tab + tab + '\t')).append("\n");
        }

        return sb.toString();
    }
}
