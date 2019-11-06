/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.web.view.AgentActiveThreadCountListSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = AgentActiveThreadCountListSerializer.class)
public class AgentActiveThreadCountList {

    private final List<AgentActiveThreadCount> agentActiveThreadRepository;

    public AgentActiveThreadCountList() {
        agentActiveThreadRepository = new ArrayList<>();
    }

    public AgentActiveThreadCountList(int initialCapacity) {
        agentActiveThreadRepository = new ArrayList<>(initialCapacity);
    }

    public void add(AgentActiveThreadCount agentActiveThreadStatus) {
        agentActiveThreadRepository.add(agentActiveThreadStatus);
    }

    public List<AgentActiveThreadCount> getAgentActiveThreadRepository() {
        // sort agentId
        agentActiveThreadRepository.sort(new Comparator<AgentActiveThreadCount>() {
            @Override
            public int compare(AgentActiveThreadCount o1, AgentActiveThreadCount o2) {
                final String agentId1 = StringUtils.defaultString(o1.getAgentId(), "");
                final String agentId2 = StringUtils.defaultString(o2.getAgentId(), "");
                return agentId1.compareTo(agentId2);
            }
        });
        return agentActiveThreadRepository;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentActiveThreadCountList{");
        sb.append("agentActiveThreadRepository=").append(agentActiveThreadRepository);
        sb.append('}');
        return sb.toString();
    }

}
