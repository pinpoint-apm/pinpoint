/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.httpclient;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class JdkHttpClientPluginConfig {

    private final boolean enable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;

    public JdkHttpClientPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.jdk.httpclient.enable", true);
        this.param = src.readBoolean("profiler.jdk.httpclient.param", true);

        boolean cookie = src.readBoolean("profiler.jdk.httpclient.cookie", false);
        DumpType cookieDumpType = src.readDumpType("profiler.jdk.httpclient.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = src.readInt("profiler.jdk.httpclient.cookie.sampling.rate", 1);
        int cookieDumpSize = src.readInt("profiler.jdk.httpclient.cookie.dumpsize", 1024);

        boolean entity = src.readBoolean("profiler.jdk.httpclient.entity", false);
        DumpType entityDumpType = src.readDumpType("profiler.jdk.httpclient.entity.dumptype", DumpType.EXCEPTION);
        int entitySamplingRate = src.readInt("profiler.jdk.httpclient.entity.sampling.rate", 1);
        int entityDumpSize = src.readInt("profiler.jdk.httpclient.entity.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);
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
        final StringBuilder sb = new StringBuilder("JdkHttpClientPluginConfig{");
        sb.append("param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}