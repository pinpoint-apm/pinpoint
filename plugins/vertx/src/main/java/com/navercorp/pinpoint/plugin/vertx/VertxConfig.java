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

import com.navercorp.pinpoint.bootstrap.config.*;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class VertxConfig {

    private final boolean enable;
    private final boolean enableHttpServer;
    private final boolean enableHttpClient;
    private final List<String> bootstrapMains;
    private final List<String> handlerBasePackageNames;

    public VertxConfig(ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config must not be null");
        }

        // plugin
        this.enable = config.readBoolean("profiler.vertx.enable", false);
        this.enableHttpServer = config.readBoolean("profiler.vertx.http.server.enable", true);
        this.enableHttpClient = config.readBoolean("profiler.vertx.http.client.enable", true);
        this.bootstrapMains = config.readList("profiler.vertx.bootstrap.main");
        this.handlerBasePackageNames = config.readList("profiler.vertx.handler.base-packages");
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnableHttpServer() {
        return enableHttpServer;
    }

    public boolean isEnableHttpClient() {
        return enableHttpClient;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public List<String> getHandlerBasePackageNames() {
        return handlerBasePackageNames;
    }
}
