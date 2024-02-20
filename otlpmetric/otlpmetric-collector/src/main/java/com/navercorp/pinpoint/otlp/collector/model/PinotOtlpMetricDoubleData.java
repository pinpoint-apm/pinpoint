/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.model;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PinotOtlpMetricDoubleData extends PinotOtlpMetricDataRow {
    private final double value;

    public PinotOtlpMetricDoubleData(ServiceId serviceId, ApplicationId applicationId,
                                     String agentId, String metricGroupName, String metricName, String fieldName,
                                     int flag, List<String> tags, String version, double value, Long eventTime, Long startTime) {
        super(serviceId, applicationId, agentId, metricGroupName, metricName, fieldName, flag, tags, version, eventTime, startTime);
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
