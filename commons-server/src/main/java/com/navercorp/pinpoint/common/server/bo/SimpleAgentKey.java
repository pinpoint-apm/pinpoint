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

package com.navercorp.pinpoint.common.server.bo;

import java.util.Objects;

/**
 * @author emeroad
 */
public class SimpleAgentKey {
    private final String agentId;
    private final long agentStartTime;

    public SimpleAgentKey(String agentId, long agentStartTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public String toString() {
        return "SimpleAgentKey{" +
                "agentId='" + agentId + '\'' +
                ", agentStartTime=" + agentStartTime +
                '}';
    }
}
