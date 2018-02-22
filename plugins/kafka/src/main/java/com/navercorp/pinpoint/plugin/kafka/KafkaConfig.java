/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class KafkaConfig {
    private boolean enable;
    private boolean createContext;
    private boolean includeHeader;
    private String caller;
    private String encoder;

    public KafkaConfig(ProfilerConfig config) {
        /*
         * kafka
         */
        this.enable = config.readBoolean("profiler.kafka.enable", false);
        this.caller = config.readString("profiler.kafka.caller", "CALLER");
        this.createContext = config.readBoolean("profiler.kafka.create.context", true);
        this.includeHeader = config.readBoolean("profiler.kafka.include.header", false);
        this.encoder = config.readString("profiler.kafka.include.encoder", "");
    }

    public boolean isEnable() {
        return enable;
    }

    public String getCaller() {
        return caller;
    }

    public boolean isCreateContext() {
        return createContext;
    }

    public boolean isIncludeHeader() {
        return includeHeader;
    }

    public String getEncoder() {
        return encoder;
    }
}
