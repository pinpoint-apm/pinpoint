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

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

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

    public VertxHttpServerConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // runtime
        this.traceRequestParam = config.readBoolean("profiler.vertx.http.server.tracerequestparam", true);
        final String tomcatExcludeURL = config.readString("profiler.vertx.http.server.excludeurl", "");
        if (!tomcatExcludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(tomcatExcludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        this.realIpHeader = config.readString("profiler.vertx.http.server.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.vertx.http.server.realipemptyvalue", null);

        final String tomcatExcludeProfileMethod = config.readString("profiler.vertx.http.server.excludemethod", "");
        if (!tomcatExcludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(tomcatExcludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
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
}