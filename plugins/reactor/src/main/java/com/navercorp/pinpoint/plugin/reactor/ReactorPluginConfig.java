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

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ReactorPluginConfig {
    private final boolean enable;
    private final boolean traceSubscribeError;
    private final List<String> traceSubscribeErrorExcludeMessageList;
    private final boolean traceSchedule;
    private final boolean traceSchedulePeriodically;

    public ReactorPluginConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // plugin
        this.enable = config.readBoolean("profiler.reactor.enable", true);
        this.traceSubscribeError = config.readBoolean("profiler.reactor.trace.subscribe.error", true);
        this.traceSubscribeErrorExcludeMessageList = config.readList("profiler.reactor.trace.subscribe.error.exclude.message");
        this.traceSchedule = config.readBoolean("profiler.reactor.trace.schedule", true);
        this.traceSchedulePeriodically = config.readBoolean("profiler.reactor.trace.schedule.periodically", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isTraceSubscribeError() {
        return traceSubscribeError;
    }

    public List<String> getTraceSubscribeErrorExcludeMessageList() {
        return traceSubscribeErrorExcludeMessageList;
    }

    public boolean isTraceSchedule() {
        return traceSchedule;
    }

    public boolean isTraceSchedulePeriodically() {
        return traceSchedulePeriodically;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReactorPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", traceSubscribeError=").append(traceSubscribeError);
        sb.append(", traceSubscribeErrorExcludeMessageList=").append(traceSubscribeErrorExcludeMessageList);
        sb.append(", traceSchedulePeriodically=").append(traceSchedulePeriodically);
        sb.append('}');
        return sb.toString();
    }
}