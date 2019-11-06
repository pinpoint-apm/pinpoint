/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ApplicationAgentHostListSerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ApplicationAgentHostListSerializer.class)
public class ApplicationAgentHostList {

    private final Map<String, List<AgentInfo>> map = new LinkedHashMap<>();

    private final int startApplicationIndex;
    private final int endApplicationIndex;
    private final int totalApplications;

    public ApplicationAgentHostList(int startApplicationIndex, int endApplicationIndex, int totalApplications) {
        this.startApplicationIndex = startApplicationIndex;
        this.endApplicationIndex = endApplicationIndex;
        this.totalApplications = totalApplications;
    }

    public int getStartApplicationIndex() {
        return startApplicationIndex;
    }

    public int getEndApplicationIndex() {
        return endApplicationIndex;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public Map<String, List<AgentInfo>> getMap() {
        return map;
    }

    public void put(String applicationName, List<AgentInfo> agentInfoList) {
        if (applicationName == null) {
            return;
        }

        List<AgentInfo> value = null;
        if (map.containsKey(applicationName)) {
            value = map.get(applicationName);
        }

        if (value == null) {
            value = new ArrayList<>();
            map.put(applicationName, value);
        }

        if (agentInfoList == null) {
            return;
        }

        for (AgentInfo agentInfo : agentInfoList) {
            if (agentInfo != null) {
                value.add(agentInfo);
            }
        }
    }

}