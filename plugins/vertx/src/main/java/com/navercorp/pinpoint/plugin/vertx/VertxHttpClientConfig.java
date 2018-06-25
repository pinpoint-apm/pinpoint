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
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientConfig {

    // client
    private boolean param = true;
    private HttpDumpConfig httpDumpConfig;
    private boolean statusCode = true;

    public VertxHttpClientConfig(ProfilerConfig config) {
        this.param = config.readBoolean("profiler.vertx.http.client.param", true);
        boolean cookie = config.readBoolean("profiler.vertx.http.client.cookie", false);
        DumpType cookieDumpType = config.readDumpType("profiler.vertx.http.client.cookie.dumptype", DumpType.EXCEPTION);
        int cookieSamplingRate = config.readInt("profiler.vertx.http.client.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.vertx.http.client.cookie.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);
        this.statusCode = config.readBoolean("profiler.vertx.http.client.entity.statuscode", true);
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VertxHttpClientConfig{");
        sb.append("param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append(", statusCode=").append(statusCode);
        sb.append('}');
        return sb.toString();
    }
}
