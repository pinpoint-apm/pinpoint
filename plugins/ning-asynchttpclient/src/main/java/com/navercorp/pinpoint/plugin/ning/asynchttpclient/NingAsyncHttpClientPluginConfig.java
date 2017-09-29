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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NingAsyncHttpClientPluginConfig {
    private final boolean enable;
    private final boolean profileCookie;
    private final DumpType cookieDumpType;
    private final int cookieDumpSize;
    private final int cookieSamplingRate;
    private final boolean profileEntity;
    private final DumpType entityDumpType;
    private final int entityDumpSize;
    private final int entitySamplingRate;
    private final boolean profileParam;
    private final DumpType paramDumpType;
    private final int paramDumpSize;
    private final int paramSamplingRate;

    public NingAsyncHttpClientPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.ning.asynchttpclient", true);

        this.profileCookie = src.readBoolean("profiler.ning.asynchttpclient.cookie", false);
        this.cookieDumpType = src.readDumpType("profiler.ning.asynchttpclient.cookie.dumptype", DumpType.EXCEPTION);
        this.cookieDumpSize = src.readInt("profiler.ning.asynchttpclient.cookie.dumpsize", 1024);
        this.cookieSamplingRate = src.readInt("profiler.ning.asynchttpclient.cookie.sampling.rate", 1);

        this.profileEntity = src.readBoolean("profiler.ning.asynchttpclient.entity", false);
        this.entityDumpType = src.readDumpType("profiler.ning.asynchttpclient.entity.dumptype", DumpType.EXCEPTION);
        this.entityDumpSize = src.readInt("profiler.ning.asynchttpclient.entity.dumpsize", 1024);
        this.entitySamplingRate = src.readInt("profiler.ning.asynchttpclient.entity.sampling.rate", 1);

        this.profileParam = src.readBoolean("profiler.ning.asynchttpclient.param", false);
        this.paramDumpType = src.readDumpType("profiler.ning.asynchttpclient.param.dumptype", DumpType.EXCEPTION);
        this.paramDumpSize = src.readInt("profiler.ning.asynchttpclient.param.dumpsize", 1024);
        this.paramSamplingRate = src.readInt("profiler.ning.asynchttpclient.param.sampling.rate", 1);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isProfileCookie() {
        return profileCookie;
    }

    public DumpType getCookieDumpType() {
        return cookieDumpType;
    }

    public int getCookieDumpSize() {
        return cookieDumpSize;
    }

    public int getCookieSamplingRate() {
        return cookieSamplingRate;
    }

    public boolean isProfileEntity() {
        return profileEntity;
    }

    public DumpType getEntityDumpType() {
        return entityDumpType;
    }

    public int getEntityDumpSize() {
        return entityDumpSize;
    }

    public int getEntitySamplingRate() {
        return entitySamplingRate;
    }

    public boolean isProfileParam() {
        return profileParam;
    }

    public DumpType getParamDumpType() {
        return paramDumpType;
    }

    public int getParamDumpSize() {
        return paramDumpSize;
    }

    public int getParamSamplingRate() {
        return paramSamplingRate;
    }
}
