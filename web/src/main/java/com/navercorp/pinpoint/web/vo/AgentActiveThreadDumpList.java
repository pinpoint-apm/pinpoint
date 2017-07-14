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
        ArrayList<AgentActiveThreadDump> copied = new ArrayList<>(agentActiveThreadDumpRepository);
        Collections.sort(copied, new Comparator<AgentActiveThreadDump>() {

            private static final int CHANGE_TO_NEW_ELEMENT = 1;
            private static final int KEEP_OLD_ELEMENT = -1;

            @Override
            public int compare(AgentActiveThreadDump oldElement, AgentActiveThreadDump newElement) {
                long diff = oldElement.getStartTime() - newElement.getStartTime();

                if (diff <= 0) {
                    return KEEP_OLD_ELEMENT;
                }

                return CHANGE_TO_NEW_ELEMENT;
            }

        });

        return Collections.unmodifiableList(copied);
    }

}
