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

/**
 * @author minwoo-jung
 */
public record HeatmapCell(long timestamp, int elapsedTime, int count) {

    public HeatmapCell(long timestamp, double elapsedTime, double count) {
        this(timestamp, (int) elapsedTime, (int) count);
    }

    @Override
    public String toString() {
        return "HeatmapCell{" +
                "timestamp=" + DateTimeFormatUtils.formatSimple(timestamp) +
                ", elapsedTime=" + elapsedTime +
                ", count=" + count +
                '}';
    }
}
