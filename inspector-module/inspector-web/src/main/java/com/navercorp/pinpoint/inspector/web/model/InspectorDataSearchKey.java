/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author minwoo.jung
 */
// TODO : (minwoo) It seems that it can be integrated into one with com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey.
// TODO : (minwoo) Shouldn't applicationName be added?
public class InspectorDataSearchKey {

    public static final String UNKNOWN_NAME = "Unknown";

    private final String tenantId;

    private final String applicationName;

    private final String agentId;

    private final String metricDefinitionId;

    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    private final int version;

    public InspectorDataSearchKey(String tenantId, String applicationName, String agentId, String metricDefinitionId, TimeWindow timeWindow, int version) {
        this.tenantId = StringPrecondition.requireHasLength(tenantId, "tenantId");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.metricDefinitionId = StringPrecondition.requireHasLength(metricDefinitionId, "metricDefinitionId");

        Objects.requireNonNull(timeWindow, "timeWindow");
        this.range = timeWindow.getWindowRange();
        this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize());
        this.limit = timeWindow.getWindowRangeCount();

        this.version = version;
    }

    public String getMetricDefinitionId() {
        return metricDefinitionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Range getRange() {
        return range;
    }

    public TimePrecision getTimePrecision() {
        return timePrecision;
    }

    public long getLimit() {
        return limit;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getVersion() {
        return version;
    }
}
