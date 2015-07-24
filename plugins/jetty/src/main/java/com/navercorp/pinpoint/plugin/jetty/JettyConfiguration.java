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
package com.navercorp.pinpoint.plugin.jetty;

import com.navercorp.pinpoint.bootstrap.config.ExcludeUrlFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class JettyConfiguration {

    private Filter<String> jettyExcludeUrlFilter;

    public JettyConfiguration(ProfilerConfig config) {
        final String jettyExcludeURL = config.readString("profiler.tomcat.excludeurl", "");
        
        if (!jettyExcludeURL.isEmpty()) {
            this.jettyExcludeUrlFilter = new ExcludeUrlFilter(jettyExcludeURL);
        }
    }

    public Filter<String> getJettyExcludeUrlFilter() {
        return jettyExcludeUrlFilter;
    }
}
