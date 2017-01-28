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
 */
public class JbossConfig {

    /** The jboss hide pinpoint header. */
    private final boolean jbossHidePinpointHeader;

    /** The jboss exclude url filter. */
    private final Filter<String> jbossExcludeUrlFilter;

    /** The jboss trace ejb. */
    private final boolean jbossTraceEjb;
    private final boolean jbossEnable;

    private final List<String> jbossBootstrapMains;
    private final boolean jbossConditionalTransformEnable;

    private final String jbossRealIpHeader;
    private final String jbossRealIpEmptyValue;
    private final boolean jbossTraceRequestParam;
    private final Filter<String> jbossExcludeProfileMethodFilter;

    /**
     * Instantiates a new jboss configuration.
     *
     * @param config the config
     */
    public JbossConfig(final ProfilerConfig config) {
        this.jbossEnable = config.readBoolean("profiler.jboss.enable", true);
        this.jbossTraceEjb = config.readBoolean("profiler.jboss.traceEjb", false);

        this.jbossBootstrapMains = config.readList("profiler.jboss.bootstrap.main");
        this.jbossConditionalTransformEnable = config.readBoolean("profiler.jboss.conditional.transform", true);
        this.jbossHidePinpointHeader = config.readBoolean("profiler.jboss.hidepinpointheader", true);

        this.jbossTraceRequestParam = config.readBoolean("profiler.jboss.tracerequestparam", true);
        final String jbossExcludeURL = config.readString("profiler.jboss.excludeurl", "");
        if (!jbossExcludeURL.isEmpty()) {
            this.jbossExcludeUrlFilter = new ExcludePathFilter(jbossExcludeURL);
        } else {
            this.jbossExcludeUrlFilter = new SkipFilter<String>();
        }
        this.jbossRealIpHeader = config.readString("profiler.jboss.realipheader", null);
        this.jbossRealIpEmptyValue = config.readString("profiler.jboss.realipemptyvalue", null);

        final String jbossExcludeProfileMethod = config.readString("profiler.jboss.excludemethod", "");
        if (!jbossExcludeProfileMethod.isEmpty()) {
            this.jbossExcludeProfileMethodFilter = new ExcludeMethodFilter(jbossExcludeProfileMethod);
        } else {
            this.jbossExcludeProfileMethodFilter = new SkipFilter<String>();
        }
    }

    public boolean isJbossEnable() {
        return jbossEnable;
    }

    public List<String> getJbossBootstrapMains() {
        return jbossBootstrapMains;
    }

    public boolean isJbossConditionalTransformEnable() {
        return jbossConditionalTransformEnable;
    }

    /**
     * Checks if is jboss hide pinpoint header.
     *
     * @return true, if is jboss hide pinpoint header
     */
    public boolean isJbossHidePinpointHeader() {
        return jbossHidePinpointHeader;
    }

    /**
     * Gets the jboss exclude url filter.
     *
     * @return the jboss exclude url filter
     */
    public Filter<String> getJbossExcludeUrlFilter() {
        return jbossExcludeUrlFilter;
    }

    /**
     * Checks if is jboss trace ejb.
     *
     * @return true, if is jboss trace ejb
     */
    public boolean isJbossTraceEjb() {
        return jbossTraceEjb;
    }


    public String getJbossRealIpHeader() {
        return jbossRealIpHeader;
    }

    public String getJbossRealIpEmptyValue() {
        return jbossRealIpEmptyValue;
    }

    public boolean isJbossTraceRequestParam() {
        return jbossTraceRequestParam;
    }

    public Filter<String> getJbossExcludeProfileMethodFilter() {
        return jbossExcludeProfileMethodFilter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JbossConfig{");
        sb.append("jbossHidePinpointHeader=").append(jbossHidePinpointHeader);
        sb.append(", jbossExcludeUrlFilter=").append(jbossExcludeUrlFilter);
        sb.append(", jbossTraceEjb=").append(jbossTraceEjb);
        sb.append(", jbossEnable=").append(jbossEnable);
        sb.append('}');
        return sb.toString();
    }
}