/**
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
package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

/**
 * @author Jongho Moon
 *
 */
public class TomcatConfiguration {
    private final boolean tomcatEnabled;
    private final boolean tomcatHidePinpointHeader;
    private final Filter<String> tomcatExcludeUrlFilter;

    public TomcatConfiguration(ProfilerConfig config) {
        this.tomcatEnabled = config.readBoolean("profiler.tomcat.enable", true);
        this.tomcatHidePinpointHeader = config.readBoolean("profiler.tomcat.hidepinpointheader", true);
        Filter<String> tomcatExcludeUrlFilter = config.getTomcatExcludeUrlFilter();
        if (tomcatExcludeUrlFilter == null) {
            this.tomcatExcludeUrlFilter = new SkipFilter<String>();
        } else {
            this.tomcatExcludeUrlFilter = tomcatExcludeUrlFilter;
        }
    }

    public boolean isTomcatEnabled() {
        return tomcatEnabled;
    }

    public Filter<String> getTomcatExcludeUrlFilter() {
        return tomcatExcludeUrlFilter;
    }

    public boolean isTomcatHidePinpointHeader() {
        return tomcatHidePinpointHeader;
    }
}