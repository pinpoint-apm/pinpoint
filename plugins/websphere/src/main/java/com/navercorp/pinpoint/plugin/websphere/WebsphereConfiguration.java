/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.websphere;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author sjmittal
 * @author jaehong.kim
 */
public class WebsphereConfiguration {

    private final boolean enable;

    private final boolean traceRequestParam;
    private final List<String> bootstrapMains;
    private final String realIpHeader;
    private final String realIpEmptyValue;

    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final boolean hidePinpointHeader;

    public WebsphereConfiguration(ProfilerConfig config) {
        Assert.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.websphere.enable", true);
        this.bootstrapMains = config.readList("profiler.websphere.bootstrap.main");
        // runtime
        this.traceRequestParam = config.readBoolean("profiler.websphere.tracerequestparam", true);
        this.realIpHeader = config.readString("profiler.websphere.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.websphere.realipemptyvalue", null);
        final String excludeURL = config.readString("profiler.websphere.excludeurl", "");
        if (!excludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(excludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        final String excludeProfileMethod = config.readString("profiler.websphere.excludemethod", "");
        if (!excludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(excludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.hidePinpointHeader = config.readBoolean("profiler.websphere.hidepinpointheader", true);
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

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebsphereConfiguration{");
        sb.append("enable=").append(enable);
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append('}');
        return sb.toString();
    }
}
