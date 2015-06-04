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
package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpClient3PluginConfig {

    private boolean apacheHttpClient3Profile = true;
    private boolean apacheHttpClient3ProfileCookie = false;
    private DumpType apacheHttpClient3ProfileCookieDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient3ProfileCookieSamplingRate = 1;
    private boolean apacheHttpClient3ProfileEntity = false;
    private DumpType apacheHttpClient3ProfileEntityDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient3ProfileEntitySamplingRate = 1;

    public HttpClient3PluginConfig(ProfilerConfig src) {
        this.apacheHttpClient3Profile = src.readBoolean("profiler.apache.httpclient3", true);
        this.apacheHttpClient3ProfileCookie = src.readBoolean("profiler.apache.httpclient3.cookie", false);
        this.apacheHttpClient3ProfileCookieDumpType = src.readDumpType("profiler.apache.httpclient3.cookie.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient3ProfileCookieSamplingRate = src.readInt("profiler.apache.httpclient3.cookie.sampling.rate", 1);

        this.apacheHttpClient3ProfileEntity = src.readBoolean("profiler.apache.httpclient3.entity", false);
        this.apacheHttpClient3ProfileEntityDumpType = src.readDumpType("profiler.apache.httpclient3.entity.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient3ProfileEntitySamplingRate = src.readInt("profiler.apache.httpclient3.entity.sampling.rate", 1);
    }

    public boolean isApacheHttpClient3Profile() {
        return apacheHttpClient3Profile;
    }

    public void setApacheHttpClient3Profile(boolean apacheHttpClient3Profile) {
        this.apacheHttpClient3Profile = apacheHttpClient3Profile;
    }

    public boolean isApacheHttpClient3ProfileCookie() {
        return apacheHttpClient3ProfileCookie;
    }

    public void setApacheHttpClient3ProfileCookie(boolean apacheHttpClient3ProfileCookie) {
        this.apacheHttpClient3ProfileCookie = apacheHttpClient3ProfileCookie;
    }

    public DumpType getApacheHttpClient3ProfileCookieDumpType() {
        return apacheHttpClient3ProfileCookieDumpType;
    }

    public void setApacheHttpClient3ProfileCookieDumpType(DumpType apacheHttpClient3ProfileCookieDumpType) {
        this.apacheHttpClient3ProfileCookieDumpType = apacheHttpClient3ProfileCookieDumpType;
    }

    public int getApacheHttpClient3ProfileCookieSamplingRate() {
        return apacheHttpClient3ProfileCookieSamplingRate;
    }

    public void setApacheHttpClient3ProfileCookieSamplingRate(int apacheHttpClient3ProfileCookieSamplingRate) {
        this.apacheHttpClient3ProfileCookieSamplingRate = apacheHttpClient3ProfileCookieSamplingRate;
    }

    public boolean isApacheHttpClient3ProfileEntity() {
        return apacheHttpClient3ProfileEntity;
    }

    public void setApacheHttpClient3ProfileEntity(boolean apacheHttpClient3ProfileEntity) {
        this.apacheHttpClient3ProfileEntity = apacheHttpClient3ProfileEntity;
    }

    public DumpType getApacheHttpClient3ProfileEntityDumpType() {
        return apacheHttpClient3ProfileEntityDumpType;
    }

    public void setApacheHttpClient3ProfileEntityDumpType(DumpType apacheHttpClient3ProfileEntityDumpType) {
        this.apacheHttpClient3ProfileEntityDumpType = apacheHttpClient3ProfileEntityDumpType;
    }

    public int getApacheHttpClient3ProfileEntitySamplingRate() {
        return apacheHttpClient3ProfileEntitySamplingRate;
    }

    public void setApacheHttpClient3ProfileEntitySamplingRate(int apacheHttpClient3ProfileEntitySamplingRate) {
        this.apacheHttpClient3ProfileEntitySamplingRate = apacheHttpClient3ProfileEntitySamplingRate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{apacheHttpClient3Profile=");
        builder.append(apacheHttpClient3Profile);
        builder.append(", apacheHttpClient3ProfileCookie=");
        builder.append(apacheHttpClient3ProfileCookie);
        builder.append(", apacheHttpClient3ProfileCookieDumpType=");
        builder.append(apacheHttpClient3ProfileCookieDumpType);
        builder.append(", apacheHttpClient3ProfileCookieSamplingRate=");
        builder.append(apacheHttpClient3ProfileCookieSamplingRate);
        builder.append(", apacheHttpClient3ProfileEntity=");
        builder.append(apacheHttpClient3ProfileEntity);
        builder.append(", apacheHttpClient3ProfileEntityDumpType=");
        builder.append(apacheHttpClient3ProfileEntityDumpType);
        builder.append(", apacheHttpClient3ProfileEntitySamplingRate=");
        builder.append(apacheHttpClient3ProfileEntitySamplingRate);
        builder.append("}");
        return builder.toString();
    }
}