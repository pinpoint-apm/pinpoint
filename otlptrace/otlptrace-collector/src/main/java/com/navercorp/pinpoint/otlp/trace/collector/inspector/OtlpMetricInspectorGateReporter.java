/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.inspector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Logs the state of the two gates of {@link OtlpMetricInspectorModule} at startup. The module itself
 * is skipped silently by its {@code @ConditionalOnProperty} when either key is off; this bean, which
 * the trace module always registers, makes "feature on, storage module off" visible instead of
 * leaving an operator to wonder why {@code /v1/metrics} answers 404.
 */
public class OtlpMetricInspectorGateReporter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final boolean featureEnabled;
    private final boolean storageEnabled;

    public OtlpMetricInspectorGateReporter(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        this.featureEnabled = environment.getProperty(OtlpMetricInspectorModule.ENABLED_KEY, boolean.class, false);
        this.storageEnabled = environment.getProperty(OtlpMetricInspectorModule.INSPECTOR_MODULE_ENABLED_KEY, boolean.class, false);
        report();
    }

    public boolean isActive() {
        return featureEnabled && storageEnabled;
    }

    private void report() {
        if (featureEnabled && storageEnabled) {
            logger.info("OTLP metrics -> Inspector receiver enabled (MetricsService/Export on the OTLP gRPC servers, POST /v1/metrics)");
        } else if (featureEnabled) {
            logger.warn("{}=true but {}=false: the Inspector storage module (Kafka/Pinot) is not in this process, "
                            + "so the OTLP metrics receiver is NOT registered. Enable the storage module or turn the feature off.",
                    OtlpMetricInspectorModule.ENABLED_KEY, OtlpMetricInspectorModule.INSPECTOR_MODULE_ENABLED_KEY);
        } else {
            logger.info("OTLP metrics -> Inspector receiver disabled ({}=false)", OtlpMetricInspectorModule.ENABLED_KEY);
        }
    }
}
