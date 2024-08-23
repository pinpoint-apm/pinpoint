/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.List;
import java.util.Objects;

public class KtorPluginConfig {

    private final boolean enable;
    private final List<String> bootstrapMains;
    private final boolean enableAsyncEndPoint;
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> traceExcludeMethodFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;

    private final boolean retransformConfigureRouting;

    public KtorPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.ktor.enable", Boolean.TRUE);
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.bootstrapMains = config.readList("profiler.ktor.http.server.bootstrap.main");
        this.enableAsyncEndPoint = config.readBoolean("profiler.ktor.http.server.end-point.async.enable", true);
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.ktor.http.server.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.ktor.http.server.excludeurl");
        this.traceExcludeMethodFilter = serverConfig.getTraceExcludeMethodFilter("profiler.ktor.http.server.trace.excludemethod");
        this.realIpHeader = serverConfig.getRealIpHeader("profiler.ktor.http.server.realipheader");
        this.realIpEmptyValue = serverConfig.getRealIpEmptyValue("profiler.ktor.http.server.realipemptyvalue");
        this.excludeProfileMethodFilter = serverConfig.getExcludeMethodFilter("profiler.ktor.http.server.excludemethod");

        this.retransformConfigureRouting = config.readBoolean("profiler.ktor.http.server.retransform.configure-routing", Boolean.TRUE);
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isEnableAsyncEndPoint() {
        return enableAsyncEndPoint;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public Filter<String> getTraceExcludeMethodFilter() {
        return traceExcludeMethodFilter;
    }

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    public boolean isRetransformConfigureRouting() {
        return retransformConfigureRouting;
    }

    @Override
    public String toString() {
        return "KtorPluginConfig{" +
                "enable=" + enable +
                ", bootstrapMains=" + bootstrapMains +
                ", enableAsyncEndPoint=" + enableAsyncEndPoint +
                ", traceRequestParam=" + traceRequestParam +
                ", excludeUrlFilter=" + excludeUrlFilter +
                ", traceExcludeMethodFilter=" + traceExcludeMethodFilter +
                ", realIpHeader='" + realIpHeader + '\'' +
                ", realIpEmptyValue='" + realIpEmptyValue + '\'' +
                ", excludeProfileMethodFilter=" + excludeProfileMethodFilter +
                ", retransformConfigureRouting=" + retransformConfigureRouting +
                '}';
    }
}
