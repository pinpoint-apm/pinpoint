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
    private final boolean markError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.jdk.httpclient.param", true);
    }

    public static HttpDumpConfig getHttpDumpConfig(ProfilerConfig config) {
        boolean cookie = config.readBoolean("profiler.jdk.httpclient.cookie", false);
        DumpType cookieDumpType = DumpType.of(config.readString("profiler.jdk.httpclient.cookie.dumptype"));
        int cookieSamplingRate = config.readInt("profiler.jdk.httpclient.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.jdk.httpclient.cookie.dumpsize", 1024);
        boolean entity = config.readBoolean("profiler.jdk.httpclient.entity", false);
        DumpType entityDumpType = DumpType.of(config.readString("profiler.jdk.httpclient.entity.dumptype"));
        int entitySamplingRate = config.readInt("profiler.jdk.httpclient.entity.sampling.rate", 1);
        int entityDumpSize = config.readInt("profiler.jdk.httpclient.entity.dumpsize", 1024);
        return HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, entity, entityDumpType, entitySamplingRate, entityDumpSize);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.jdk.httpclient.mark.error", true);
    }

    public JdkHttpClientPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.jdk.httpclient.enable", true);

        this.param = isParam(src);
        this.httpDumpConfig = getHttpDumpConfig(src);
        this.markError = isMarkError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "JdkHttpClientPluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", httpDumpConfig=" + httpDumpConfig +
                ", markError=" + markError +
                '}';
    }
}