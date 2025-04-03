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

import com.navercorp.pinpoint.web.heatmap.vo.HeatMapMetricCell;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class HeatMapMetricCellView {

    private final HeatMapMetricCell heatMapMetricCell;

    public HeatMapMetricCellView(HeatMapMetricCell heatMapMetricCell) {
        this.heatMapMetricCell = Objects.requireNonNull(heatMapMetricCell, "heatMapMetricCell");
    }

    public int getRow() {
        return heatMapMetricCell.getRow();
    }

    public int getElapsedTime() {
        return heatMapMetricCell.getElapsedTime();
    }

    public int getSuccessCount() {
        return heatMapMetricCell.getSuccessCount();
    }

    public int getFailCount() {
        return heatMapMetricCell.getFailCount();
    }
}
