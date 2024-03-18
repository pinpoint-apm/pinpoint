/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class HttpClient5PluginConfig {
    private boolean enable;
    private boolean param = true;
    private boolean statusCode = true;
    private boolean io;
    private HttpDumpConfig httpDumpConfig;

    public HttpClient5PluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.apache.httpclient5.enable", true);
        this.param = src.readBoolean("profiler.apache.httpclient5.param", true);

        boolean cookie = src.readBoolean("profiler.apache.httpclient5.cookie", false);
        DumpType cookieDumpType = src.readDumpType("profiler.apache.httpclient5.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = src.readInt("profiler.apache.httpclient5.cookie.sampling.rate", 1);
        int cookieDumpSize = src.readInt("profiler.apache.httpclient5.cookie.dumpsize", 1024);
        boolean entity = src.readBoolean("profiler.apache.httpclient5.entity", false);
        DumpType entityDumpType = src.readDumpType("profiler.apache.httpclient5.entity.dumptype", DumpType.EXCEPTION);
        int entitySamplingRate = src.readInt("profiler.apache.httpclient5.entity.sampling.rate", 1);
        int entityDumpSize = src.readInt("profiler.apache.httpclient5.entity.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);

        this.statusCode = src.readBoolean("profiler.apache.httpclient5.entity.statuscode", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isParam() {
        return param;
    }

    public boolean isStatusCode() {
        return statusCode;
    }

    public boolean isIo() {
        return io;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    @Override
    public String toString() {
        return "HttpClient5PluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", statusCode=" + statusCode +
                ", io=" + io +
                ", httpDumpConfig=" + httpDumpConfig +
                '}';
    }
}