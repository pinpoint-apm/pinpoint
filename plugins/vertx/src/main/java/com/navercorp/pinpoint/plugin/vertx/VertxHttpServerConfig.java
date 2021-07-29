/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class VertxHttpServerConfig {
    // server
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;
    private final boolean hidePinpointHeader;
    private final String requestHandlerMethodName;

    public VertxHttpServerConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        this.requestHandlerMethodName = config.readString("profiler.vertx.http.server.request-handler.method.name", "io.vertx.ext.web.impl.RouterImpl.accept");
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.vertx.http.server.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.vertx.http.server.excludeurl");
        this.realIpHeader = serverConfig.getRealIpHeader("profiler.vertx.http.server.realipheader");
        this.realIpEmptyValue = serverConfig.getRealIpEmptyValue("profiler.vertx.http.server.realipemptyvalue");
        this.excludeProfileMethodFilter = serverConfig.getExcludeMethodFilter("profiler.vertx.http.server.excludemethod");
        this.hidePinpointHeader = serverConfig.isHidePinpointHeader("profiler.vertx.http.server.hidepinpointheader");
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

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    public String getRequestHandlerMethodName() {
        return requestHandlerMethodName;
    }
}