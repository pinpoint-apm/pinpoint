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
package com.navercorp.pinpoint.plugin.google.httpclient;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpClientPluginConfig {

    private final boolean async;

    public HttpClientPluginConfig(ProfilerConfig src) {
        async = src.readBoolean("profiler.google.httpclient.async", true);
    }

    public boolean isAsync() {
        return async;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{async=");
        builder.append(async);
        builder.append("}");
        return builder.toString();
    }
}