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

package com.navercorp.pinpoint.plugin.jboss;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

import java.util.List;

/**
 * The Class JbossConfig.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 * @author jaehong.kim
 */
public class JbossConfig {

    /**
     * The jboss hide pinpoint header.
     */
    private final boolean hidePinpointHeader;

    /**
     * The jboss exclude url filter.
     */
    private final Filter<String> excludeUrlFilter;

    /**
     * The jboss trace ejb.
     */
    private final boolean traceEjb;
    private final boolean enable;

    private final List<String> bootstrapMains;

    private final String realIpHeader;
    private final String realIpEmptyValue;
    private final boolean traceRequestParam;
    private final Filter<String> excludeProfileMethodFilter;

    /**
     * Instantiates a new jboss configuration.
     *
     * @param config the config
     */
    public JbossConfig(final ProfilerConfig config) {
        this.enable = config.readBoolean("profiler.jboss.enable", true);
        this.traceEjb = config.readBoolean("profiler.jboss.traceEjb", false);

        this.bootstrapMains = config.readList("profiler.jboss.bootstrap.main");
        this.hidePinpointHeader = config.readBoolean("profiler.jboss.hidepinpointheader", true);

        this.traceRequestParam = config.readBoolean("profiler.jboss.tracerequestparam", true);
        final String jbossExcludeURL = config.readString("profiler.jboss.excludeurl", "");
        if (!jbossExcludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(jbossExcludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        this.realIpHeader = config.readString("profiler.jboss.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.jboss.realipemptyvalue", null);

        final String jbossExcludeProfileMethod = config.readString("profiler.jboss.excludemethod", "");
        if (!jbossExcludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(jbossExcludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    /**
     * Checks if is jboss hide pinpoint header.
     *
     * @return true, if is jboss hide pinpoint header
     */
    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    /**
     * Gets the jboss exclude url filter.
     *
     * @return the jboss exclude url filter
     */
    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    /**
     * Checks if is jboss trace ejb.
     *
     * @return true, if is jboss trace ejb
     */
    public boolean isTraceEjb() {
        return traceEjb;
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
        final StringBuilder sb = new StringBuilder("JbossConfig{");
        sb.append("hidePinpointHeader=").append(hidePinpointHeader);
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", traceEjb=").append(traceEjb);
        sb.append(", enable=").append(enable);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append('}');
        return sb.toString();
    }
}