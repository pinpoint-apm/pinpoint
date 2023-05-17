/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class Echo {

    private final ClusterKey agentKey;
    private final String message;

    public Echo(ClusterKey agentKey, String message) {
        this.agentKey = agentKey;
        this.message = message;
    }

    public ClusterKey getAgentKey() {
        return agentKey;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Echo echo = (Echo) o;
        return Objects.equals(agentKey, echo.agentKey) && Objects.equals(message, echo.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentKey, message);
    }

    @Override
    public String toString() {
        return "Echo{" +
                "agentKey=" + agentKey +
                ", message='" + message + '\'' +
                '}';
    }

}
