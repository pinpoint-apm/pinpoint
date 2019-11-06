/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class HttpClient3PluginConfig {

    private final boolean param;
    private final boolean io;
    private final HttpDumpConfig httpDumpConfig;

    public HttpClient3PluginConfig(ProfilerConfig src) {
        this.param = src.readBoolean("profiler.apache.httpclient3.param", true);

        boolean cookie = src.readBoolean("profiler.apache.httpclient3.cookie", false);
        DumpType cookieDumpType = src.readDumpType("profiler.apache.httpclient3.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = src.readInt("profiler.apache.httpclient3.cookie.sampling.rate", 1);
        int cookieDumpSize = src.readInt("profiler.apache.httpclient3.cookie.dumpsize", 1024);

        boolean entity = src.readBoolean("profiler.apache.httpclient3.entity", false);
        DumpType entityDumpType = src.readDumpType("profiler.apache.httpclient3.entity.dumptype", DumpType.EXCEPTION);
        int entitySamplingRate = src.readInt("profiler.apache.httpclient3.entity.sampling.rate", 1);
        int entityDumpSize = src.readInt("profiler.apache.httpclient3.entity.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);

        this.io = src.readBoolean("profiler.apache.httpclient3.io", true);
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isIo() {
        return io;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpClient3PluginConfig{");
        sb.append("param=").append(param);
        sb.append(", io=").append(io);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}