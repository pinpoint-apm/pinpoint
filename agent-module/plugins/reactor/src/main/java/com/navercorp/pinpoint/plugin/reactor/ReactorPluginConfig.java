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

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ReactorPluginConfig {
    private final boolean enable;

    private final boolean traceOnError;
    private final boolean tracePublishOn;
    private final boolean traceSubscribeOn;
    private final boolean traceDelay;
    private final boolean traceInterval;
    private final boolean traceRetry;
    private final boolean traceTimeout;
    private final boolean traceSubscribe;
    private final boolean markErrorRetry;
    private final boolean markErrorOnError;

    public ReactorPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.reactor.enable", true);
        this.traceOnError = config.readBoolean("profiler.reactor.trace.onError", false);
        this.markErrorOnError = config.readBoolean("profiler.reactor.mark.error.onError", false);
        this.tracePublishOn = config.readBoolean("profiler.reactor.trace.publishOn", true);
        this.traceSubscribeOn = config.readBoolean("profiler.reactor.trace.subscribeOn", true);
        this.traceDelay = config.readBoolean("profiler.reactor.trace.delay", true);
        this.traceInterval = config.readBoolean("profiler.reactor.trace.interval", true);

        this.traceRetry = config.readBoolean("profiler.reactor.trace.retry", true);
        this.markErrorRetry = config.readBoolean("profiler.reactor.mark.error.retry", false);

        this.traceTimeout = config.readBoolean("profiler.reactor.trace.timeout", true);
        this.traceSubscribe = config.readBoolean("profiler.reactor.trace.subscribe", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isTraceOnError() {
        return traceOnError;
    }

    public boolean isTracePublishOn() {
        return tracePublishOn;
    }

    public boolean isTraceSubscribeOn() {
        return traceSubscribeOn;
    }

    public boolean isTraceDelay() {
        return traceDelay;
    }

    public boolean isTraceInterval() {
        return traceInterval;
    }

    public boolean isTraceRetry() {
        return traceRetry;
    }

    public boolean isTraceTimeout() {
        return traceTimeout;
    }

    public boolean isTraceSubscribe() {
        return traceSubscribe;
    }

    public boolean isMarkErrorRetry() {
        return markErrorRetry;
    }

    public boolean isMarkErrorOnError() {
        return markErrorOnError;
    }

    @Override
    public String toString() {
        return "ReactorPluginConfig{" +
                "enable=" + enable +
                ", traceOnError=" + traceOnError +
                ", tracePublishOn=" + tracePublishOn +
                ", traceSubscribeOn=" + traceSubscribeOn +
                ", traceDelay=" + traceDelay +
                ", traceInterval=" + traceInterval +
                ", traceRetry=" + traceRetry +
                ", traceTimeout=" + traceTimeout +
                ", traceSubscribe=" + traceSubscribe +
                ", markErrorRetry=" + markErrorRetry +
                ", markErrorOnError=" + markErrorOnError +
                '}';
    }
}