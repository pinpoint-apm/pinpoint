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

import java.util.List;

/**
 * @author minwoo-jung
 */
public class HeatMapMetricCell {
    private final int row;
    private final int elapsedTime;
    private int successCount;
    private int failCount;

    public HeatMapMetricCell(int row, int elapsedTime) {
        this.row = row;
        this.elapsedTime = elapsedTime;
        this.successCount = 0;
        this.failCount = 0;
    }

    public void updateSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void updateFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getRow() {
        return row;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public String prettyToString(String tab) {
        return tab + "{\n" +
                tab + "\t row=" + row + ",\n" +
                tab + "\t elapsedTime=" + elapsedTime + ",\n" +
                tab + "\t successCount=" + successCount + ",\n" +
                tab + "\t failCount=" + failCount + ",\n" +
                tab + "}\n";
    }
}
