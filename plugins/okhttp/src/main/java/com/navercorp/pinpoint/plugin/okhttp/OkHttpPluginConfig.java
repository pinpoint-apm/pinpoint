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
package com.navercorp.pinpoint.plugin.okhttp;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class OkHttpPluginConfig {

    private final boolean enable;
    private final boolean param;
    private final boolean statusCode;
    private final boolean async;
    private HttpDumpConfig httpDumpConfig;

    public OkHttpPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.okhttp.enable", true);
        this.param = src.readBoolean("profiler.okhttp.param", false);

        boolean cookie = src.readBoolean("profiler.okhttp.cookie", false);
        DumpType cookieDumpType = src.readDumpType("profiler.okhttp.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = src.readInt("profiler.okhttp.cookie.sampling.rate", 1);
        int cookieDumpSize = src.readInt("profiler.okhttp.cookie.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);

        this.statusCode = src.readBoolean("profiler.okhttp.entity.statuscode", true);
        this.async = src.readBoolean("profiler.okhttp.async", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isParam() {
        return param;
    }

    public boolean isAsync() {
        return async;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OkHttpPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", param=").append(param);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", async=").append(async);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}