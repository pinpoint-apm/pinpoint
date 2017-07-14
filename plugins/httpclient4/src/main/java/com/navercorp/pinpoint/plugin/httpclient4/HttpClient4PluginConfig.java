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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpClient4PluginConfig {

    /**
     * apache http client 4
     */
    private boolean param = true;
    private boolean cookie = false;
    private DumpType cookieDumpType = DumpType.EXCEPTION;
    private int cookieSamplingRate = 1;
    private boolean entity = false;
    private DumpType entityDumpType = DumpType.EXCEPTION;
    private int entitySamplingRate = 1;
    private boolean statusCode = true;
    private boolean io;

    public HttpClient4PluginConfig(ProfilerConfig src) {
        /*
         * apache http client 4
         */
        this.param = src.readBoolean("profiler.apache.httpclient4.param", true);
        this.cookie = src.readBoolean("profiler.apache.httpclient4.cookie", false);
        this.cookieDumpType = src.readDumpType("profiler.apache.httpclient4.cookie.dumptype", DumpType.EXCEPTION);
        this.cookieSamplingRate = src.readInt("profiler.apache.httpclient4.cookie.sampling.rate", 1);

        this.entity = src.readBoolean("profiler.apache.httpclient4.entity", false);
        this.entityDumpType = src.readDumpType("profiler.apache.httpclient4.entity.dumptype", DumpType.EXCEPTION);
        this.entitySamplingRate = src.readInt("profiler.apache.httpclient4.entity.sampling.rate", 1);

        this.statusCode = src.readBoolean("profiler.apache.httpclient4.entity.statuscode", true);
        this.io = src.readBoolean("profiler.apache.httpclient4.io", true);
    }

    public boolean isParam() {
        return param;
    }

    public boolean isCookie() {
        return cookie;
    }

    public DumpType getCookieDumpType() {
        return cookieDumpType;
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

    public boolean isIo() {
        return io;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpClient4PluginConfig{");
        sb.append("param=").append(param);
        sb.append(", cookie=").append(cookie);
        sb.append(", cookieDumpType=").append(cookieDumpType);
        sb.append(", cookieSamplingRate=").append(cookieSamplingRate);
        sb.append(", entity=").append(entity);
        sb.append(", entityDumpType=").append(entityDumpType);
        sb.append(", entitySamplingRate=").append(entitySamplingRate);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", io=").append(io);
        sb.append('}');
        return sb.toString();
    }
}
