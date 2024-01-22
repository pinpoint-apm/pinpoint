/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo.jung
 */
public class MetricDataSearchKey {

    private final String tenantId;
    private final String hostGroupName;
    private final String hostName;
    private final String metricName;
    private final String metricDefinitionId;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    public MetricDataSearchKey(String tenantId, String hostGroupName, String hostName, String metricName, String metricDefinitionId, TimeWindow timeWindow) {
        this.tenantId = StringPrecondition.requireHasLength(tenantId, "tenantId");
        this.hostGroupName = StringPrecondition.requireHasLength(hostGroupName, "hostGroupName");
        this.hostName = StringPrecondition.requireHasLength(hostName, "hostName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricDefinitionId = StringPrecondition.requireHasLength(metricDefinitionId, "metricDefinitionId");
        Objects.requireNonNull(timeWindow, "timeWindow");
        this.range = timeWindow.getWindowRange();
        this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize());
        this.limit = timeWindow.getWindowRangeCount();
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMetricName() {
        return metricName;
    }

    public Range getRange() {
        return range;
    }

    public long getLimit() {
        return limit;
    }

    public TimePrecision getTimePrecision() {
        return timePrecision;
    }

    public String getMetricDefinitionId() {
        return metricDefinitionId;
    }

}
