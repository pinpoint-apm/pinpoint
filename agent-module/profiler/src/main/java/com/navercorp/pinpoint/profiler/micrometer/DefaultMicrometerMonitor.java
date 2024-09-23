/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.micrometer;

import com.navercorp.pinpoint.profiler.micrometer.config.MicrometerConfig;
import io.micrometer.registry.otlp.OtlpConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * MicrometerMonitor
 *
 */
public class DefaultMicrometerMonitor implements MicrometerMonitor {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentOtlpMeterRegistry registry;

    public DefaultMicrometerMonitor(String applicationName,
                                    String agentId,
                                    MicrometerConfig config) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(config, "config");

        if (!config.isEnable()) {
            throw new IllegalStateException("Micrometer is not enabled");
        }

        OtlpConfig otlpConfig = AgentOtlpConfig.getOtlpConfig(
                config.getUrl(),
                config.getStep(),
                config.getBatchSize(),
                "default",
                applicationName,
                agentId);
        this.registry = new AgentOtlpMeterRegistry(otlpConfig);
    }


    @Override
    public void start() {
        logger.info("MicrometerMonitor started");
    }

    @Override
    public void stop() {
        logger.info("MicrometerMonitor stopped");
        registry.close();
    }

}
