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
    private HttpDumpConfig httpDumpConfig;
    private boolean markError;
    private boolean traceFutureError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient5.param", Boolean.TRUE);
    }

    public static HttpDumpConfig getHttpDumpConfig(ProfilerConfig config) {
        boolean cookie = config.readBoolean("profiler.apache.httpclient5.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.apache.httpclient5.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.apache.httpclient5.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.apache.httpclient5.cookie.dumpsize", 1024);
        boolean entity = config.readBoolean("profiler.apache.httpclient5.entity", false);
        DumpType entityDumpType = config.readDumpType("profiler.apache.httpclient5.entity.dumptype", DumpType.EXCEPTION);
        int entitySamplingRate = config.readInt("profiler.apache.httpclient5.entity.sampling.rate", 1);
        int entityDumpSize = config.readInt("profiler.apache.httpclient5.entity.dumpsize", 1024);

        return HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);
    }

    public static boolean isStatusCode(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient5.entity.statuscode", Boolean.TRUE);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient5.mark.error", Boolean.FALSE);
    }

    public static boolean isTraceFutureError(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient5.trace.future.error", Boolean.FALSE);
    }

    public HttpClient5PluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.apache.httpclient5.enable", true);
        this.param = isParam(src);
        this.httpDumpConfig = getHttpDumpConfig(src);
        this.statusCode = isStatusCode(src);
        this.markError = isMarkError(src);
        this.traceFutureError = isTraceFutureError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "HttpClient5PluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", statusCode=" + statusCode +
                ", httpDumpConfig=" + httpDumpConfig +
                ", markError=" + markError +
                ", traceFutureError=" + traceFutureError +
                '}';
    }
}