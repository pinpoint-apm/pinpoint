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

package com.navercorp.pinpoint.web.vo;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ApplicationAgentList {

    private final String groupName;
    private final List<AgentInfo> agentInfos;

    public ApplicationAgentList(String groupName, List<AgentInfo> agentInfos) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.agentInfos = Objects.requireNonNull(agentInfos, "agentInfos");
    }

    public String getGroupName() {
        return groupName;
    }

    public List<AgentInfo> getAgentInfos() {
        return agentInfos;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append('\'').append(groupName).append('\'');
        sb.append(":").append(agentInfos);
        sb.append('}');
        return sb.toString();
    }
}
