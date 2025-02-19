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
package com.navercorp.pinpoint.plugin.pulsar;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author zhouzixin@apache.org
 */
public class PulsarConfig {

    private static final String ENABLE = "profiler.pulsar.enable";
    private static final String PRODUCER_ENABLE = "profiler.pulsar.producer.enable";
    private static final String CONSUMER_ENABLE = "profiler.pulsar.consumer.enable";

    private final boolean enable;
    private final boolean producerEnable;
    private final boolean consumerEnable;

    public PulsarConfig(ProfilerConfig cfg) {
        enable = cfg.readBoolean(ENABLE, true);
        producerEnable = cfg.readBoolean(PRODUCER_ENABLE, false);
        consumerEnable = cfg.readBoolean(CONSUMER_ENABLE, false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isProducerEnable() {
        return producerEnable;
    }

    public boolean isConsumerEnable() {
        return consumerEnable;
    }

    @Override
    public String toString() {
        return "PulsarConfig{" +
            "enable=" + enable +
            ", producerEnable=" + producerEnable +
            ", consumerEnable=" + consumerEnable +
            '}';
    }
}
