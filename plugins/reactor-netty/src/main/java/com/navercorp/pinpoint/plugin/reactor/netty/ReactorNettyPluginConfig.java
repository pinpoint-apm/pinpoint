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
import java.util.Objects;

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
    private final boolean clientEnable;
    private boolean param = true;
    private final boolean forceSample;
    //Sampling percentage,max is 100.(1: 1%, 2: 2%,50: 50%)
    private final int forceSampleRate;

    public ReactorNettyPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

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
        // Client
        this.clientEnable = config.readBoolean("profiler.reactor-netty.client.enable", true);
        this.param = config.readBoolean("profiler.reactor-netty.client.param", true);

        // Reactor
        this.traceSubscribeError = config.readBoolean("profiler.reactor-netty.trace.subscribe.error", true);
        this.traceSubscribeErrorExcludeMessageList = config.readList("profiler.reactor-netty.trace.subscribe.error.exclude.message");

        this.forceSample = config.readBoolean("profiler.reactor-netty.forceSample.enable", true);
        this.forceSampleRate = config.readInt("profiler.reactor-netty.forceSample.rate", 100);
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

    public boolean isClientEnable() {
        return clientEnable;
    }

    public boolean isParam() {
        return param;
    }

    public boolean isForceSample() {
        return forceSample;
    }

    public int getForceSampleRate() {
        return forceSampleRate;
    }
    @Override
    public String toString() {
        return "ReactorNettyPluginConfig{" +
                "enable=" + enable +
                ", bootstrapMains=" + bootstrapMains +
                ", traceRequestParam=" + traceRequestParam +
                ", excludeUrlFilter=" + excludeUrlFilter +
                ", realIpHeader='" + realIpHeader + '\'' +
                ", realIpEmptyValue='" + realIpEmptyValue + '\'' +
                ", excludeProfileMethodFilter=" + excludeProfileMethodFilter +
                ", enableAsyncEndPoint=" + enableAsyncEndPoint +
                ", traceSubscribeError=" + traceSubscribeError +
                ", traceSubscribeErrorExcludeMessageList=" + traceSubscribeErrorExcludeMessageList +
                ", clientEnable=" + clientEnable +
                ", param=" + param +
                ", forceSample=" + forceSample +
                ", forceSampleRate=" + forceSampleRate +
                '}';
    }
}
