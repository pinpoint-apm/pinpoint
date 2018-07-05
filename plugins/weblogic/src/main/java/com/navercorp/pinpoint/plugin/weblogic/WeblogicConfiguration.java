/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.weblogic;

import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

/**
 * @author andyspan
 * @author jaehong.kim
 */
public class WeblogicConfiguration {
    private final boolean enable;
    private final List<String> bootstrapMains;
    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final boolean traceRequestParam;
    private final boolean hidePinpointHeader;

    public WeblogicConfiguration(ProfilerConfig config) {
        this.enable = config.readBoolean("profiler.weblogic.enable", true);
        this.bootstrapMains = config.readList("profiler.weblogic.bootstrap.main");
        final String weblogicExcludeURL = config.readString("profiler.weblogic.excludeurl", "");

        this.realIpHeader = config.readString("profiler.weblogic.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.weblogic.realipemptyvalue", null);
        if (!weblogicExcludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(weblogicExcludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        final String excludeProfileMethod = config.readString("profiler.weblogic.excludemethod", "");
        if (!excludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(excludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.traceRequestParam = config.readBoolean("profiler.weblogic.tracerequestparam", true);
        this.hidePinpointHeader = config.readBoolean("profiler.weblogic.hidepinpointheader", true);
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getWeblgoicBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeblogicConfiguration{");
        sb.append("enable=").append(enable);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append('}');
        return sb.toString();
    }
}
