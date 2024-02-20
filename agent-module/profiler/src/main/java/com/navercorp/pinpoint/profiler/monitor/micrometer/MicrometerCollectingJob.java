/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Properties;

public class MicrometerCollectingJob {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final OtlpMeterRegistry meterRegistry;

    public MicrometerCollectingJob(String micrometerUrl, String micrometerStep, String micrometerBatchSize, String applicationName, String agentId) {
        this.meterRegistry = new OtlpMeterRegistry(getOtlpConfig(micrometerUrl, micrometerStep, micrometerBatchSize, "", applicationName, agentId), Clock.SYSTEM);
        bindMetrics();
    }

    private void bindMetrics() {
        // jvm
        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmCompilationMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmHeapPressureMetrics().bindTo(meterRegistry);
        new JvmInfoMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);

        // system
        new DiskSpaceMetrics(new File("/")).bindTo(meterRegistry);  // add other paths with user input?
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
    }

    private OtlpConfig getOtlpConfig(String micrometerUrl, String micrometerStep, String micrometerBatchSize,
                                     String serviceName, String applicationName, String agentId) {
        Properties propertiesConfig = new Properties();
        propertiesConfig.put("otlp.url", micrometerUrl);
        propertiesConfig.put("otlp.step", String.valueOf(micrometerStep));
        propertiesConfig.put("otlp.batchSize", String.valueOf(micrometerBatchSize));
        propertiesConfig.put("otlp.resourceAttributes", "service.namespace=" + serviceName + ",service.name=" + applicationName + ",pinpoint.agentId=" + agentId);
        OtlpConfig otlpConfig = (key -> (String) propertiesConfig.get(key));
        return otlpConfig;
    }
}