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

package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;

/**
 * 
 * @author baiyang
 *
 */
public class ResinConfig {

    private final boolean resinEnabled;
    private final String resinBootstrapMain;
    private final boolean resinTraceRequestParam;
    private final boolean resinTraceCookies;
    private final Filter<String> resinExcludeUrlFilter;

    public ResinConfig(ProfilerConfig config) {
        this.resinEnabled = config.readBoolean("profiler.resin.enable", true);
        this.resinBootstrapMain = config.readString("profiler.resin.bootstrap.main", "com.caucho.server.resin.Resin");
        this.resinTraceRequestParam = config.readBoolean("profiler.resin.tracerequestparam", true);
        this.resinTraceCookies = config.readBoolean("profiler.resin.tracecookies", true);

        final String resinExcludeURL = config.readString("profiler.resin.excludeurl", "");

        if (!resinExcludeURL.isEmpty()) {
            this.resinExcludeUrlFilter = new ExcludePathFilter(resinExcludeURL);
        } else {
            this.resinExcludeUrlFilter = new SkipFilter<String>();
        }
    }

    public boolean isResinEnabled() {
        return resinEnabled;
    }

    public Filter<String> getResinExcludeUrlFilter() {
        return resinExcludeUrlFilter;
    }

    public String getResinBootstrapMain() {
        return resinBootstrapMain;
    }

    public boolean isResinTraceRequestParam() {
        return resinTraceRequestParam;
    }

    public boolean isResinTraceCookies() {
        return resinTraceCookies;
    }

}