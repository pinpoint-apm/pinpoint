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
package com.navercorp.pinpoint.plugin.axis2.jaxws;

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

public class Axis2JaxWsConfiguration {

    private final Filter<String> axis2JaxWsExcludeUrlFilter;

    public Axis2JaxWsConfiguration(ProfilerConfig config) {
        final String axis2JaxWsExcludeURL = config.readString("profiler.axis2JaxWs.excludeurl", "");

        if (!axis2JaxWsExcludeURL.isEmpty()) {
            this.axis2JaxWsExcludeUrlFilter = new ExcludePathFilter(axis2JaxWsExcludeURL);
        } else{
            this.axis2JaxWsExcludeUrlFilter = new  SkipFilter<String>();
        }
    }

    public Filter<String> getAxis2JaxWsExcludeUrlFilter() {
        return axis2JaxWsExcludeUrlFilter;
    }
}
