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
    private final HttpDumpConfig httpDumpConfig;
    private final boolean markError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.okhttp.param", false);
    }

    public static HttpDumpConfig getHttpDumpConfig(ProfilerConfig config) {
        boolean cookie = config.readBoolean("profiler.okhttp.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.okhttp.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.okhttp.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.okhttp.cookie.dumpsize", 1024);
        return HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);

    }

    public static boolean isStatusCode(ProfilerConfig config) {
        return config.readBoolean("profiler.okhttp.entity.statuscode", true);
    }

    public static boolean isAsync(ProfilerConfig config) {
        return config.readBoolean("profiler.okhttp.async", true);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.okhttp.mark.error", Boolean.TRUE);
    }

    public OkHttpPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.okhttp.enable", true);

        this.param = isParam(src);
        this.httpDumpConfig = getHttpDumpConfig(src);
        this.statusCode = isStatusCode(src);
        this.async = isAsync(src);
        this.markError = isMarkError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "OkHttpPluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", statusCode=" + statusCode +
                ", async=" + async +
                ", httpDumpConfig=" + httpDumpConfig +
                ", markError=" + markError +
                '}';
    }
}