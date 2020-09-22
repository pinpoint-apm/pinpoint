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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.AgentActiveThreadDumpListSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = AgentActiveThreadDumpListSerializer.class)
public class AgentActiveThreadDumpList {

    public static final AgentActiveThreadDumpList EMPTY_INSTANCE = new AgentActiveThreadDumpList(0);

    private final List<AgentActiveThreadDump> agentActiveThreadDumpRepository;

    public AgentActiveThreadDumpList() {
        this(4);
    }

    public AgentActiveThreadDumpList(int initialCapacity) {
        agentActiveThreadDumpRepository = new ArrayList<>(initialCapacity);
    }

    public void add(AgentActiveThreadDump agentActiveThreadStatus) {
        agentActiveThreadDumpRepository.add(agentActiveThreadStatus);
    }

    public List<AgentActiveThreadDump> getAgentActiveThreadDumpRepository() {
        return Collections.unmodifiableList(agentActiveThreadDumpRepository);
    }

    public List<AgentActiveThreadDump> getSortOldestAgentActiveThreadDumpRepository() {
        List<AgentActiveThreadDump> copied = new ArrayList<>(agentActiveThreadDumpRepository);
        copied.sort(Comparator.comparingLong(AgentActiveThreadDump::getStartTime));
        return Collections.unmodifiableList(copied);
    }

}
