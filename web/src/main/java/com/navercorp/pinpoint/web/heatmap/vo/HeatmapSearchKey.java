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

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimePrecision;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo-jung
 */
public class HeatmapSearchKey {
    private final String tenantId;
    private final String applicationName;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    public HeatmapSearchKey(String tenantId, String applicationName, TimeWindow timeWindow) {
        this.tenantId = StringPrecondition.requireHasLength(tenantId, "tenantId");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");

        Objects.requireNonNull(timeWindow, "timeWindow");
        this.range = timeWindow.getWindowRange();
        this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize());
        this.limit = timeWindow.getWindowRangeCount();
    }
}
