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

package com.navercorp.pinpoint.otlp.web.frontend.export;

import com.navercorp.pinpoint.common.server.frontend.export.FrontendConfigExporter;
import com.navercorp.pinpoint.otlp.web.config.OtlpMetricProperties;

import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class OtlpMetricPropertiesExporter implements FrontendConfigExporter {

    private final OtlpMetricProperties otlpMetricProperties;

    public OtlpMetricPropertiesExporter(OtlpMetricProperties otlpMetricProperties) {
        this.otlpMetricProperties = Objects.requireNonNull(otlpMetricProperties, "otlpMetricProperties");
    }

    @Override
    public void export(Map<String, Object> export) {
        export.put("periodMax.otlpMetric", otlpMetricProperties.getOtlpMetricPeriodMax());
    }
}
