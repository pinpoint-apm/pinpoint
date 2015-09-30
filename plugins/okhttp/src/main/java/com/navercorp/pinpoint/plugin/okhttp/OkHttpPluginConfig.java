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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class OkHttpPluginConfig {

    private boolean cookie = false;
    private DumpType cookieDumpType = DumpType.EXCEPTION;
    private int cookieSamplingRate = 1;
    private boolean entity = false;
    private DumpType entityDumpType = DumpType.EXCEPTION;
    private int entitySamplingRate = 1;
    private boolean statusCode = true;

    private final boolean async;

    public OkHttpPluginConfig(ProfilerConfig src) {
        this.cookie = src.readBoolean("profiler.okhttp.cookie", false);
        this.cookieDumpType = src.readDumpType("profiler.okhttp.cookie.dumptype", DumpType.EXCEPTION);
        this.cookieSamplingRate = src.readInt("profiler.okhttp.cookie.sampling.rate", 1);

        this.entity = src.readBoolean("profiler.okhttp.entity", false);
        this.entityDumpType = src.readDumpType("profiler.okhttp.entity.dumptype", DumpType.EXCEPTION);
        this.entitySamplingRate = src.readInt("profiler.okhttp.entity.sampling.rate", 1);

        this.statusCode = src.readBoolean("profiler.okhttp.entity.statuscode", true);
        this.async = src.readBoolean("profiler.okhttp.async", true);
    }

    public boolean isAsync() {
        return async;
    }

    public DumpType getCookieDumpType() {
        return cookieDumpType;
    }

    public boolean isCookie() {
        return cookie;
    }

    public int getCookieSamplingRate() {
        return cookieSamplingRate;
    }

    public boolean isEntity() {
        return entity;
    }

    public DumpType getEntityDumpType() {
        return entityDumpType;
    }

    public int getEntitySamplingRate() {
        return entitySamplingRate;
    }

    public boolean isStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OkHttpPluginConfig{");
        sb.append("cookie=").append(cookie);
        sb.append(", cookieDumpType=").append(cookieDumpType);
        sb.append(", cookieSamplingRate=").append(cookieSamplingRate);
        sb.append(", entity=").append(entity);
        sb.append(", entityDumpType=").append(entityDumpType);
        sb.append(", entitySamplingRate=").append(entitySamplingRate);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", async=").append(async);
        sb.append('}');
        return sb.toString();
    }
}