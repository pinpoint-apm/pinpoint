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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ApplicationAgentListSerializer;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ApplicationAgentListSerializer.class)
public class ApplicationAgentList {

    public enum Key {
        APPLICATION_NAME {
            @Override
            public String getKey(AgentInfo agentInfo) {
                return agentInfo.getApplicationName();
            }
        },
        HOST_NAME {
            @Override
            public String getKey(AgentInfo agentInfo) {
                return agentInfo.getHostName();
            }
        };

        public abstract String getKey(AgentInfo agentInfo);
    }

    private final SortedMap<String, List<AgentInfo>> applicationAgentList;

    public ApplicationAgentList() {
        this.applicationAgentList = new TreeMap<>();
    }

    public ApplicationAgentList(SortedMap<String, List<AgentInfo>> applicationAgentList) {
        if (applicationAgentList == null) {
            throw new NullPointerException("applicationAgentList must not be null");
        }
        this.applicationAgentList = applicationAgentList;
    }

    public void merge(ApplicationAgentList applicationAgentList) {
        for (Map.Entry<String, List<AgentInfo>> e : applicationAgentList.getApplicationAgentList().entrySet()) {
            String key = e.getKey();
            if (this.applicationAgentList.containsKey(key)) {
                this.applicationAgentList.get(key).addAll(e.getValue());
            } else {
                this.applicationAgentList.put(key, e.getValue());
            }
        }
    }

    public SortedMap<String, List<AgentInfo>> getApplicationAgentList() {
        return this.applicationAgentList;
    }

}
