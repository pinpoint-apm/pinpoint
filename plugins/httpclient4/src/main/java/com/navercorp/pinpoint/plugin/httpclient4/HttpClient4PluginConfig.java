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
    private boolean apacheHttpClient4ProfileCookie = false;
    private DumpType apacheHttpClient4ProfileCookieDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileCookieSamplingRate = 1;
    private boolean apacheHttpClient4ProfileEntity = false;
    private DumpType apacheHttpClient4ProfileEntityDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileEntitySamplingRate = 1;
    private boolean apacheHttpClient4ProfileStatusCode = true;

    /**
     * apache nio http client
     */
    private boolean apacheNIOHttpClient4Profile = true;

    public HttpClient4PluginConfig(ProfilerConfig src) {
        /**
         * apache http client 4
         */
        this.apacheHttpClient4ProfileCookie = src.readBoolean("profiler.apache.httpclient4.cookie", false);
        this.apacheHttpClient4ProfileCookieDumpType = src.readDumpType("profiler.apache.httpclient4.cookie.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileCookieSamplingRate = src.readInt("profiler.apache.httpclient4.cookie.sampling.rate", 1);

        this.apacheHttpClient4ProfileEntity = src.readBoolean("profiler.apache.httpclient4.entity", false);
        this.apacheHttpClient4ProfileEntityDumpType = src.readDumpType("profiler.apache.httpclient4.entity.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileEntitySamplingRate = src.readInt("profiler.apache.httpclient4.entity.sampling.rate", 1);

        this.apacheHttpClient4ProfileStatusCode = src.readBoolean("profiler.apache.httpclient4.entity.statuscode", true);
        /**
         * apache nio http client
         */
        this.apacheNIOHttpClient4Profile = src.readBoolean("profiler.apache.nio.httpclient4", true);
    }

    public boolean isApacheHttpClient4ProfileCookie() {
        return apacheHttpClient4ProfileCookie;
    }

    public DumpType getApacheHttpClient4ProfileCookieDumpType() {
        return apacheHttpClient4ProfileCookieDumpType;
    }

    public int getApacheHttpClient4ProfileCookieSamplingRate() {
        return apacheHttpClient4ProfileCookieSamplingRate;
    }

    public boolean isApacheHttpClient4ProfileEntity() {
        return apacheHttpClient4ProfileEntity;
    }

    public DumpType getApacheHttpClient4ProfileEntityDumpType() {
        return apacheHttpClient4ProfileEntityDumpType;
    }

    public int getApacheHttpClient4ProfileEntitySamplingRate() {
        return apacheHttpClient4ProfileEntitySamplingRate;
    }

    public boolean isApacheHttpClient4ProfileStatusCode() {
        return apacheHttpClient4ProfileStatusCode;
    }

    public boolean isApacheNIOHttpClient4Profile() {
        return apacheNIOHttpClient4Profile;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{apacheHttpClient4ProfileCookie=");
        builder.append(apacheHttpClient4ProfileCookie);
        builder.append(", apacheHttpClient4ProfileCookieDumpType=");
        builder.append(apacheHttpClient4ProfileCookieDumpType);
        builder.append(", apacheHttpClient4ProfileCookieSamplingRate=");
        builder.append(apacheHttpClient4ProfileCookieSamplingRate);
        builder.append(", apacheHttpClient4ProfileEntity=");
        builder.append(apacheHttpClient4ProfileEntity);
        builder.append(", apacheHttpClient4ProfileEntityDumpType=");
        builder.append(apacheHttpClient4ProfileEntityDumpType);
        builder.append(", apacheHttpClient4ProfileEntitySamplingRate=");
        builder.append(apacheHttpClient4ProfileEntitySamplingRate);
        builder.append(", apacheHttpClient4ProfileStatusCode=");
        builder.append(apacheHttpClient4ProfileStatusCode);
        builder.append(", apacheNIOHttpClient4Profile=");
        builder.append(apacheNIOHttpClient4Profile);
        builder.append("}");
        return builder.toString();
    }
}
