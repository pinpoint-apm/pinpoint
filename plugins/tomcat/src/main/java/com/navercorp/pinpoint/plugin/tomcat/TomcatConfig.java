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
 *
 */

package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TomcatConfig {

    private final boolean tomcatEnable;
    private final List<String> tomcatBootstrapMains;
    private final boolean tomcatConditionalTransformEnable;
    private final boolean tomcatHidePinpointHeader;

    private final boolean tomcatTraceRequestParam;
    private final Filter<String> tomcatExcludeUrlFilter;
    private final String tomcatRealIpHeader;
    private final String tomcatRealIpEmptyValue;
    private final Filter<String> tomcatExcludeProfileMethodFilter;

    // for transform conditional check
    private final List<String> springBootBootstrapMains;

    public TomcatConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // plugin
        this.tomcatEnable = config.readBoolean("profiler.tomcat.enable", true);
        this.tomcatBootstrapMains = config.readList("profiler.tomcat.bootstrap.main");
        this.tomcatConditionalTransformEnable = config.readBoolean("profiler.tomcat.conditional.transform", true);
        this.tomcatHidePinpointHeader = config.readBoolean("profiler.tomcat.hidepinpointheader", true);

        // runtime
        this.tomcatTraceRequestParam = config.readBoolean("profiler.tomcat.tracerequestparam", true);
        final String tomcatExcludeURL = config.readString("profiler.tomcat.excludeurl", "");
        if (!tomcatExcludeURL.isEmpty()) {
            this.tomcatExcludeUrlFilter = new ExcludePathFilter(tomcatExcludeURL);
        } else {
            this.tomcatExcludeUrlFilter = new SkipFilter<String>();
        }
        this.tomcatRealIpHeader = config.readString("profiler.tomcat.realipheader", null);
        this.tomcatRealIpEmptyValue = config.readString("profiler.tomcat.realipemptyvalue", null);

        final String tomcatExcludeProfileMethod = config.readString("profiler.tomcat.excludemethod", "");
        if (!tomcatExcludeProfileMethod.isEmpty()) {
            this.tomcatExcludeProfileMethodFilter = new ExcludeMethodFilter(tomcatExcludeProfileMethod);
        } else {
            this.tomcatExcludeProfileMethodFilter = new SkipFilter<String>();
        }

        this.springBootBootstrapMains = config.readList("profiler.springboot.bootstrap.main");
    }

    public boolean isTomcatEnable() {
        return tomcatEnable;
    }

    public List<String> getTomcatBootstrapMains() {
        return tomcatBootstrapMains;
    }

    public boolean isTomcatConditionalTransformEnable() {
        return tomcatConditionalTransformEnable;
    }

    public boolean isTomcatHidePinpointHeader() {
        return tomcatHidePinpointHeader;
    }

    public boolean isTomcatTraceRequestParam() {
        return tomcatTraceRequestParam;
    }

    public Filter<String> getTomcatExcludeUrlFilter() {
        return tomcatExcludeUrlFilter;
    }

    public String getTomcatRealIpHeader() {
        return tomcatRealIpHeader;
    }

    public String getTomcatRealIpEmptyValue() {
        return tomcatRealIpEmptyValue;
    }

    public Filter<String> getTomcatExcludeProfileMethodFilter() {
        return tomcatExcludeProfileMethodFilter;
    }

    public List<String> getSpringBootBootstrapMains() {
        return springBootBootstrapMains;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TomcatConfig{");
        sb.append("tomcatEnable=").append(tomcatEnable);
        sb.append(", tomcatBootstrapMains=").append(tomcatBootstrapMains);
        sb.append(", tomcatConditionalTransformEnable=").append(tomcatConditionalTransformEnable);
        sb.append(", tomcatHidePinpointHeader=").append(tomcatHidePinpointHeader);
        sb.append(", tomcatTraceRequestParam=").append(tomcatTraceRequestParam);
        sb.append(", tomcatExcludeUrlFilter=").append(tomcatExcludeUrlFilter);
        sb.append(", tomcatRealIpHeader='").append(tomcatRealIpHeader).append('\'');
        sb.append(", tomcatRealIpEmptyValue='").append(tomcatRealIpEmptyValue).append('\'');
        sb.append(", tomcatExcludeProfileMethodFilter=").append(tomcatExcludeProfileMethodFilter);
        sb.append(", springBootBootstrapMains=").append(springBootBootstrapMains);
        sb.append('}');
        return sb.toString();
    }
}
