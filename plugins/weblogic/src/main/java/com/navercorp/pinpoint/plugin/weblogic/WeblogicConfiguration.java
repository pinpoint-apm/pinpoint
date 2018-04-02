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

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
/**
 * 
 * @author andyspan
 *
 */
public class WeblogicConfiguration {
    private final boolean weblogicEnabled;
    private final List<String> weblogicBootstrapMains;
	private final Filter<String> weblogicExcludeUrlFilter;
	private final boolean traceRequestParam;

	public WeblogicConfiguration(ProfilerConfig config) {
        this.weblogicEnabled = config.readBoolean("profiler.weblogic.enable", true);
        this.weblogicBootstrapMains = config.readList("profiler.weblogic.bootstrap.main");
        final String weblogicExcludeURL = config.readString("profiler.weblogic.excludeurl", "");

		if (!weblogicExcludeURL.isEmpty()) {
			this.weblogicExcludeUrlFilter = new ExcludePathFilter(weblogicExcludeURL);
		} else {
			this.weblogicExcludeUrlFilter = new SkipFilter<String>();
		}
		this.traceRequestParam = config.readBoolean("profiler.weblogic.tracerequestparam", true);
	}

	public Filter<String> getWeblogicExcludeUrlFilter() {
		return weblogicExcludeUrlFilter;
	}
	
    public boolean isWeblogicEnabled() {
        return weblogicEnabled;
    }

    public List<String> getWeblgoicBootstrapMains() {
        return weblogicBootstrapMains;
}

	public boolean isTraceRequestParam() {
		return traceRequestParam;
	}
}
