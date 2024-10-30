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
package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class HttpClient4PluginConfig {

    private boolean enable = true;
    private boolean param = true;
    private boolean statusCode = true;
    private boolean io;
    private HttpDumpConfig httpDumpConfig;
    private boolean markError;
    private boolean traceFutureError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient4.param", true);
    }

    public static HttpDumpConfig getHttpDumpConfig(ProfilerConfig config) {
        boolean cookie = config.readBoolean("profiler.apache.httpclient4.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.apache.httpclient4.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.apache.httpclient4.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.apache.httpclient4.cookie.dumpsize", 1024);
        boolean entity = config.readBoolean("profiler.apache.httpclient4.entity", false);
        DumpType entityDumpType = config.readDumpType("profiler.apache.httpclient4.entity.dumptype", DumpType.EXCEPTION);
        int entitySamplingRate = config.readInt("profiler.apache.httpclient4.entity.sampling.rate", 1);
        int entityDumpSize = config.readInt("profiler.apache.httpclient4.entity.dumpsize", 1024);
        return HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);
    }

    public static boolean isStatusCode(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient4.entity.statuscode", true);
    }

    public static boolean isIo(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient4.io", true);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient4.mark.error", Boolean.TRUE);
    }

    public static boolean isTraceFutureError(ProfilerConfig config) {
        return config.readBoolean("profiler.apache.httpclient4.trace.future.error", Boolean.TRUE);
    }

    public HttpClient4PluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.apache.httpclient4.enable", true);

        this.param = isParam(src);
        this.httpDumpConfig = getHttpDumpConfig(src);
        this.statusCode = isStatusCode(src);
        this.io = isIo(src);
        this.markError = isMarkError(src);
        this.traceFutureError = isTraceFutureError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "HttpClient4PluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", statusCode=" + statusCode +
                ", io=" + io +
                ", httpDumpConfig=" + httpDumpConfig +
                ", markError=" + markError +
                ", traceFutureError=" + traceFutureError +
                '}';
    }
}