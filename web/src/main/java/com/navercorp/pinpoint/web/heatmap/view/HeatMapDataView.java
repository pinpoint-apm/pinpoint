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

package com.navercorp.pinpoint.web.heatmap.view;

import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricColumn;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapSize;
import com.navercorp.pinpoint.web.heatmap.vo.HeatmapSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatMapDataView {
    private final HeatMapData heatmapData;

    public HeatMapDataView(HeatMapData heatMapData) {
        this.heatmapData = Objects.requireNonNull(heatMapData, "heatMapData");;
    }

    public HeatmapSize getSize() {
        return heatmapData.getHeatmapSize();
    }

    public HeatmapSummary getSummary() {
        return heatmapData.getHeatmapSummary();
    }

    public List<HeatMapMetricColumnView> getHeatmapData() {
        List<HeatMapMetricColumn> heatMapMetricColumnList = heatmapData.getDescHeatMapMetricColumnList();

        List<HeatMapMetricColumnView> heatMapMetricColumnViewList = new ArrayList<>(heatMapMetricColumnList.size());

        heatMapMetricColumnList.stream()
                                .map(HeatMapMetricColumnView::new)
                                .forEach(heatMapMetricColumnViewList::add);

        return heatMapMetricColumnViewList;

    }
}
