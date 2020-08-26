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

package com.navercorp.pinpoint.plugin.reactor.netty;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ReactorNettyPluginConfig {
    private final boolean enable;
    private final List<String> bootstrapMains;
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;
    private final boolean enableAsyncEndPoint;
    private final boolean traceSubscribeError;
    private final List<String> traceSubscribeErrorExcludeMessageList;

    public ReactorNettyPluginConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // plugin
        this.enable = config.readBoolean("profiler.reactor-netty.enable", true);
        this.bootstrapMains = config.readList("profiler.reactor-netty.server.bootstrap.main");
        this.enableAsyncEndPoint = config.readBoolean("profiler.reactor-netty.server.end-point.async.enable", true);
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.reactor-netty.server.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.reactor-netty.server.excludeurl");
        this.realIpHeader = serverConfig.getRealIpHeader("profiler.reactor-netty.server.realipheader");
        this.realIpEmptyValue = serverConfig.getRealIpEmptyValue("profiler.reactor-netty.server.realipemptyvalue");
        this.excludeProfileMethodFilter = serverConfig.getExcludeMethodFilter("profiler.reactor-netty.server.excludemethod");
        // Reactor
        this.traceSubscribeError = config.readBoolean("profiler.reactor-netty.trace.subscribe.error", true);
        this.traceSubscribeErrorExcludeMessageList = config.readList("profiler.reactor-netty.trace.subscribe.error.exclude.message");
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
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

    public boolean isEnableAsyncEndPoint() {
        return enableAsyncEndPoint;
    }

    public boolean isTraceSubscribeError() {
        return traceSubscribeError;
    }

    public List<String> getTraceSubscribeErrorExcludeMessageList() {
        return traceSubscribeErrorExcludeMessageList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReactorNettyPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append(", enableAsyncEndPoint=").append(enableAsyncEndPoint);
        sb.append(", traceSubscribeError=").append(traceSubscribeError);
        sb.append(", traceSubscribeErrorExcludeMessageList=").append(traceSubscribeErrorExcludeMessageList);
        sb.append('}');
        return sb.toString();
    }
}
