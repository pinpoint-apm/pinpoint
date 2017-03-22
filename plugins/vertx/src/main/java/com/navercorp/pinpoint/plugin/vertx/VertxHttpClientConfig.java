/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientConfig {

    // client
    private boolean param = true;
    private boolean cookie = false;
    private DumpType cookieDumpType = DumpType.EXCEPTION;
    private int cookieSamplingRate = 1;
    private boolean statusCode = true;

    public VertxHttpClientConfig(ProfilerConfig config) {
        this.param = config.readBoolean("profiler.vertx.http.client.param", true);
        this.cookie = config.readBoolean("profiler.vertx.http.client.cookie", false);
        this.cookieDumpType = config.readDumpType("profiler.vertx.http.client.cookie.dumptype", DumpType.EXCEPTION);
        this.cookieSamplingRate = config.readInt("profiler.vertx.http.client.cookie.sampling.rate", 1);

        this.statusCode = config.readBoolean("profiler.vertx.http.client.entity.statuscode", true);
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

    public boolean isStatusCode() {
        return statusCode;
    }
}
