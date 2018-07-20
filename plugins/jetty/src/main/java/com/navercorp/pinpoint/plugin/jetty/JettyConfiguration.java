/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jetty;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

import java.util.List;

/**
 * @author Chaein Jung
 * @author jaehong.kim
 */
public class JettyConfiguration {

    private final boolean enable;
    private final List<String> bootstrapMains;
    private final Filter<String> excludeUrlFilter;
    private final boolean hidePinpointHeader;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final boolean traceRequestParam;
    private final Filter<String> excludeProfileMethodFilter;

    public JettyConfiguration(ProfilerConfig config) {
        this.enable = config.readBoolean("profiler.jetty.enable", true);
        this.bootstrapMains = config.readList("profiler.jetty.bootstrap.main");
        final String jettyExcludeURL = config.readString("profiler.jetty.excludeurl", "");

        if (!jettyExcludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(jettyExcludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        boolean hidePinpointHeader = config.readBoolean("profiler.jetty.hide-pinpoint-header", true);
        if (hidePinpointHeader) {
            hidePinpointHeader = config.readBoolean("profiler.jetty.hidepinpointheader", true);
        }
        this.hidePinpointHeader = hidePinpointHeader;
        final String excludeProfileMethod = config.readString("profiler.jetty.excludemethod", "");
        if (!excludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(excludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.traceRequestParam = config.readBoolean("profiler.jetty.tracerequestparam", true);
        this.realIpHeader = config.readString("profiler.jetty.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.jetty.realipemptyvalue", null);
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JettyConfiguration{");
        sb.append("enable=").append(enable);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append('}');
        return sb.toString();
    }
}
