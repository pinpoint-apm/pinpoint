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

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * The Class JbossConfiguration.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 */
public class JbossConfiguration {

    /** The jboss hide pinpoint header. */
    private final boolean jbossHidePinpointHeader;

    /** The jboss exclude url filter. */
    private final Filter<String> jbossExcludeUrlFilter;

    /** The jboss trace ejb. */
    private final boolean jbossTraceEjb;

    /**
     * Instantiates a new jboss configuration.
     *
     * @param config the config
     */
    public JbossConfiguration(final ProfilerConfig config) {
        this.jbossHidePinpointHeader = config.isJbossHidePinpointHeader();
        this.jbossExcludeUrlFilter = config.getJbossExcludeUrlFilter();
        this.jbossTraceEjb = config.isJbossTraceEjb();
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

}