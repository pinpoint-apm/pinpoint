/*
 *  Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AkkaHttpConfig {

    private static final String KEY_ENABLE = "profiler.akka.http.enable";
    private static final String KEY_EXCLUDEURL = "profiler.akka.http.excludeurl";
    private static final String KEY_IP_HEADER = "profiler.akka.http.realipheader";
    private static final String KEY_EXCLUDE_HTTP_METHOD = "profiler.akka.http.excludemethod";
    static final String KEY_TRANSFORM_TARGET_NAME = "profiler.akka.http.transform.targetname";
    static final String KEY_TRANSFORM_PARAMETERS = "profiler.akka.http.transform.targetparameter";

    private static final boolean DEFAULT_ENABLE = false;
    private static final String DEFAULT_TRANSFORM_TARGET_NAME = "akka.http.scaladsl.server.directives.BasicDirectives.$anonfun$mapRequestContext$2";

    // server
    private final boolean enable;
    private final String realIpHeader;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeHttpMethodFilter;
    private final String transformTargetName;
    private final List<String> transformParameters;

    public AkkaHttpConfig(ProfilerConfig config) {
        Assert.requireNonNull(config, "config");

        this.enable = config.readBoolean(KEY_ENABLE, DEFAULT_ENABLE);

        this.realIpHeader = config.readString(KEY_IP_HEADER, null);

        final String excludeUrl = config.readString(KEY_EXCLUDEURL, "");
        if (!excludeUrl.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(excludeUrl);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }

        final String excludeHttpMethodType = config.readString(KEY_EXCLUDE_HTTP_METHOD, "");
        if (!excludeHttpMethodType.isEmpty()) {
            this.excludeHttpMethodFilter = new ExcludeMethodFilter(excludeHttpMethodType);
        } else {
            this.excludeHttpMethodFilter = new SkipFilter<String>();
        }

        this.transformTargetName = config.readString(KEY_TRANSFORM_TARGET_NAME, DEFAULT_TRANSFORM_TARGET_NAME);
        this.transformParameters = config.readList(KEY_TRANSFORM_PARAMETERS);
    }

    public boolean isEnable() {
        return enable;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
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

    public List<String> getTransformTargetParameters() { return transformParameters; }

    @Override
    public String toString() {
        return "AkkaHttpConfig{" +
                "enable=" + enable +
                ", realIpHeader='" + realIpHeader + '\'' +
                ", excludeUrlFilter=" + excludeUrlFilter +
                ", excludeHttpMethodFilter=" + excludeHttpMethodFilter +
                ", transformTargetName='" + transformTargetName + '\'' +
                ", transformParameters=" + transformParameters +
                '}';
    }
}
