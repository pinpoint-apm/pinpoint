/*
 *  Copyright 2026 NAVER Corp.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.pekko.http;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ServerConfig;

import java.util.List;
import java.util.Objects;

public class PekkoHttpConfig {

    static final String KEY_TRANSFORM_TARGET_NAME = "profiler.pekko.http.transform.targetname";

    private static final String KEY_TRANSFORM_PARAMETERS = "profiler.pekko.http.transform.targetparameter";
    private static final String KEY_ENABLE = "profiler.pekko.http.enable";
    private static final String KEY_EXCLUDEURL = "profiler.pekko.http.excludeurl";
    private static final String KEY_TRACE_EXCLUDEMETHOD = "profiler.pekko.http.trace.excludemethod";
    private static final String KEY_IP_HEADER = "profiler.pekko.http.realipheader";
    private static final String KEY_EXCLUDE_HTTP_METHOD = "profiler.pekko.http.excludemethod";
    private static final boolean DEFAULT_ENABLE = false;
    private static final String DEFAULT_TRANSFORM_TARGET_NAME = "org.apache.pekko.http.scaladsl.server.directives.BasicDirectives.$anonfun$mapRequestContext$2";

    // server
    private final boolean enable;
    private final String realIpHeader;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> traceExcludeMethodFilter;
    private final Filter<String> excludeHttpMethodFilter;
    private final String transformTargetName;
    private final List<String> transformParameters;

    public PekkoHttpConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        this.enable = config.readBoolean(KEY_ENABLE, DEFAULT_ENABLE);
        this.transformTargetName = config.readString(KEY_TRANSFORM_TARGET_NAME, DEFAULT_TRANSFORM_TARGET_NAME);
        this.transformParameters = config.readList(KEY_TRANSFORM_PARAMETERS);
        // Server
        final ServerConfig serverConfig = new ServerConfig(config);
        this.realIpHeader = serverConfig.getRealIpHeader(KEY_IP_HEADER);
        this.excludeUrlFilter = serverConfig.getExcludeUrlFilter(KEY_EXCLUDEURL);
        this.traceExcludeMethodFilter = serverConfig.getTraceExcludeMethodFilter(KEY_TRACE_EXCLUDEMETHOD);
        this.excludeHttpMethodFilter = serverConfig.getExcludeMethodFilter(KEY_EXCLUDE_HTTP_METHOD);
    }

    public boolean isEnable() {
        return enable;
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

    public Filter<String> getExcludeHttpMethodFilter() {
        return excludeHttpMethodFilter;
    }

    public String getTransformTargetName() {
        return transformTargetName;
    }

    public List<String> getTransformTargetParameters() {
        return transformParameters;
    }

    @Override
    public String toString() {
        return "PekkoHttpConfig{" +
                "enable=" + enable +
                ", realIpHeader='" + realIpHeader + '\'' +
                ", excludeUrlFilter=" + excludeUrlFilter +
                ", traceExcludeMethodFilter=" + traceExcludeMethodFilter +
                ", excludeHttpMethodFilter=" + excludeHttpMethodFilter +
                ", transformTargetName='" + transformTargetName + '\'' +
                ", transformParameters=" + transformParameters +
                '}';
    }
}
