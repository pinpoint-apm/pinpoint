/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.undertow;

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class UndertowConfig {

    private final boolean enable;
    private final List<String> bootstrapMains;
    private final boolean hidePinpointHeader;
    private final boolean traceRequestParam;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> traceExcludeMethodFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final Filter<String> excludeProfileMethodFilter;
    private final boolean deployServlet;
    private final Filter<String> httpHandlerClassNameFilter;

    public UndertowConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.undertow.enable", true);
        this.deployServlet = config.readBoolean("profiler.undertow.deploy.servlet", true);
        this.bootstrapMains = config.readList("profiler.undertow.bootstrap.main");
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.hidePinpointHeader = serverConfig.isHidePinpointHeader("profiler.undertow.hidepinpointheader");
        this.traceRequestParam = serverConfig.isTraceRequestParam("profiler.undertow.tracerequestparam");
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter("profiler.undertow.excludeurl");
        this.traceExcludeMethodFilter = serverConfig.getTraceExcludeMethodFilter("profiler.undertow.trace.excludemethod");
        this.realIpHeader = serverConfig.getRealIpHeader("profiler.undertow.realipheader");
        this.realIpEmptyValue = serverConfig.getRealIpEmptyValue("profiler.undertow.realipemptyvalue");
        this.excludeProfileMethodFilter = serverConfig.getExcludeMethodFilter("profiler.undertow.excludemethod");

        final String httpHandlerClassName = config.readString("profiler.undertow.http-handler.class.name", "");
        if (!httpHandlerClassName.isEmpty()) {
            this.httpHandlerClassNameFilter = new ExcludePathFilter(httpHandlerClassName, ".", ",");
        } else {
            this.httpHandlerClassNameFilter = new Filter<String>() {
                @Override
                public boolean filter(String value) {
                    return true;
                }
            };
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isDeployServlet() {
        return deployServlet;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
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

    public Filter<String> getHttpHandlerClassNameFilter() {
        return httpHandlerClassNameFilter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UndertowConfig{");
        sb.append("enable=").append(enable);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", traceExcludeMethodFilter=").append(traceExcludeMethodFilter);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append('}');
        return sb.toString();
    }
}
