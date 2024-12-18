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
package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class NingAsyncHttpClientPluginConfig {
    private final boolean enable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;
    private final boolean markError;

    public static boolean isParam(ProfilerConfig config) {
        return config.readBoolean("profiler.ning.asynchttpclient.param", false);
    }

    public static HttpDumpConfig getHttpDumpConfig(ProfilerConfig config) {
        boolean profileCookie = config.readBoolean("profiler.ning.asynchttpclient.cookie", false);
        DumpType cookieDumpType = DumpType.of(config.readString("profiler.ning.asynchttpclient.cookie.dumptype"));
        int cookieDumpSize = config.readInt("profiler.ning.asynchttpclient.cookie.dumpsize", 1024);
        int cookieSamplingRate = config.readInt("profiler.ning.asynchttpclient.cookie.sampling.rate", 1);
        boolean profileEntity = config.readBoolean("profiler.ning.asynchttpclient.entity", false);
        DumpType entityDumpType = DumpType.of(config.readString("profiler.ning.asynchttpclient.entity.dumptype"));
        int entityDumpSize = config.readInt("profiler.ning.asynchttpclient.entity.dumpsize", 1024);
        int entitySamplingRate = config.readInt("profiler.ning.asynchttpclient.entity.sampling.rate", 1);
        return HttpDumpConfig.get(profileCookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, profileEntity, entityDumpType, entitySamplingRate, entityDumpSize);
    }

    public static boolean isMarkError(ProfilerConfig config) {
        return config.readBoolean("profiler.ning.asynchttpclient.mark.error", Boolean.TRUE);
    }

    public NingAsyncHttpClientPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.ning.asynchttpclient", true);

        this.param = isParam(src);
        this.httpDumpConfig = getHttpDumpConfig(src);
        this.markError = isMarkError(src);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "NingAsyncHttpClientPluginConfig{" +
                "enable=" + enable +
                ", param=" + param +
                ", httpDumpConfig=" + httpDumpConfig +
                ", markError=" + markError +
                '}';
    }
}
