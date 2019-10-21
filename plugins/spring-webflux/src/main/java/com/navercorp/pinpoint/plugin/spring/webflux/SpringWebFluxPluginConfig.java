/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.webflux;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxPluginConfig {
    private final boolean enable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;

    public SpringWebFluxPluginConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config");
        }

        this.enable = config.readBoolean("profiler.spring.webflux.enable", true);
        // Client
        this.param = config.readBoolean("profiler.spring.webflux.client.param", true);
        boolean cookie = config.readBoolean("profiler.spring.webflux.client.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.spring.webflux.client.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.spring.webflux.client.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.spring.webflux.client.cookie.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringWebFluxPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}
