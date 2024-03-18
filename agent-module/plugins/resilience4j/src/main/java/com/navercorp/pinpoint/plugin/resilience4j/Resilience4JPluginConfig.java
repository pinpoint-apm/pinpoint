/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.resilience4j;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class Resilience4JPluginConfig {
    private final boolean enable;
    private final boolean traceCircuitBreaker;
    private final boolean markErrorCircuitBreaker;

    public Resilience4JPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.resilience4j.enable", true);
        this.traceCircuitBreaker = config.readBoolean("profiler.resilience4j.trace.circuit-breaker", true);
        this.markErrorCircuitBreaker = config.readBoolean("profiler.resilience4j.mark.error.circuit-breaker", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isTraceCircuitBreaker() {
        return traceCircuitBreaker;
    }

    public boolean isMarkErrorCircuitBreaker() {
        return markErrorCircuitBreaker;
    }

    @Override
    public String toString() {
        return "Resilience4JPluginConfig{" +
                "enable=" + enable +
                ", traceCircuitBreaker=" + traceCircuitBreaker +
                ", markErrorCircuitBreaker=" + markErrorCircuitBreaker +
                '}';
    }
}