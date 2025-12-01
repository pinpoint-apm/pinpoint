/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.config;

import com.navercorp.pinpoint.profiler.name.IdSourceType;

import java.util.Objects;
import java.util.Properties;

public class AgentSystemConfig {

    private final String agentId;
    private final String version;

    public AgentSystemConfig(String agentId, String version) {
        this.agentId = agentId;
        this.version = version;
    }


    public void dump(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        properties.setProperty(IdSourceType.SYSTEM.getAgentId(), agentId);
        properties.setProperty("pinpoint.version", version);
    }

    public String getAgentId() {
        return agentId;
    }

    public String getVersion() {
        return version;
    }
}
