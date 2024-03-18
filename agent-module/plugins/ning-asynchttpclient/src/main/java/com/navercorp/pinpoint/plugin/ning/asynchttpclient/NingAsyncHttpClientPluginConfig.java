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

    public NingAsyncHttpClientPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.ning.asynchttpclient", true);
        this.param = src.readBoolean("profiler.ning.asynchttpclient.param", false);
        boolean profileCookie = src.readBoolean("profiler.ning.asynchttpclient.cookie", false);
        DumpType cookieDumpType = src.readDumpType("profiler.ning.asynchttpclient.cookie.dumptype", DumpType.EXCEPTION);
        int cookieDumpSize = src.readInt("profiler.ning.asynchttpclient.cookie.dumpsize", 1024);
        int cookieSamplingRate = src.readInt("profiler.ning.asynchttpclient.cookie.sampling.rate", 1);
        boolean profileEntity = src.readBoolean("profiler.ning.asynchttpclient.entity", false);
        DumpType entityDumpType = src.readDumpType("profiler.ning.asynchttpclient.entity.dumptype", DumpType.EXCEPTION);
        int entityDumpSize = src.readInt("profiler.ning.asynchttpclient.entity.dumpsize", 1024);
        int entitySamplingRate = src.readInt("profiler.ning.asynchttpclient.entity.sampling.rate", 1);
        this.httpDumpConfig = HttpDumpConfig.get(profileCookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, profileEntity, entityDumpType, entitySamplingRate, entityDumpSize);
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
        final StringBuilder sb = new StringBuilder("NingAsyncHttpClientPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append('}');
        return sb.toString();
    }
}
