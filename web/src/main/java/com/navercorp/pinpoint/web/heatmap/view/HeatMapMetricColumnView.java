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

package com.navercorp.pinpoint.web.heatmap.view;

import com.google.common.collect.Iterators;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricCell;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricColumn;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatMapMetricColumnView {

    private final HeatMapMetricColumn heatMapMetricColumn;

    public HeatMapMetricColumnView(HeatMapMetricColumn heatMapMetricColumn) {
        this.heatMapMetricColumn = Objects.requireNonNull(heatMapMetricColumn, "heatMapMetricColumn");
    }

    public int getColumn() {
        return heatMapMetricColumn.column();
    }

    public long getTimestamp() {
        return heatMapMetricColumn.timestamp();
    }

    public Iterator<HeatMapMetricCellView> getCellDataList() {
        List<HeatMapMetricCell> heatMapMetricCellList = heatMapMetricColumn.getHeatMapMetricCellList();
        return Iterators.transform(heatMapMetricCellList.iterator(), HeatMapMetricCellView::new);
    }
}
