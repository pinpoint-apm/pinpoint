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

package com.navercorp.pinpoint.web.vo.activethread;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.AgentActiveThreadCountListSerializer;
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

    public AgentActiveThreadCountList(int initialCapacity) {
        agentActiveThreadRepository = new ArrayList<>(initialCapacity);
    }

    public void add(AgentActiveThreadCount agentActiveThreadStatus) {
        agentActiveThreadRepository.add(agentActiveThreadStatus);
    }

    public List<AgentActiveThreadCount> getAgentActiveThreadRepository() {
        // sort agentId
        agentActiveThreadRepository.sort(Comparator.comparing(threadCount -> StringUtils.defaultString(threadCount.getAgentId())));

        return agentActiveThreadRepository;
    }

    @Override
    public String toString() {
        return "AgentActiveThreadCountList{" + "agentActiveThreadRepository=" + agentActiveThreadRepository + '}';
    }

}
