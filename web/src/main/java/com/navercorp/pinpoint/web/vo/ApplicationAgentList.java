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

import java.util.List;
import java.util.SortedMap;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.view.ApplicationAgentListSerializer;

/**
 * @author minwoo.jung
 */
@JsonSerialize(using = ApplicationAgentListSerializer.class)
public class ApplicationAgentList {

    private MatcherGroup matcherGroup = new MatcherGroup();
    
    SortedMap<String, List<AgentInfoBo>> applicationAgentList;
    
    public ApplicationAgentList(SortedMap<String, List<AgentInfoBo>> applicationAgentList, MatcherGroup matcherGroup) {
        if (matcherGroup != null) {
            this.matcherGroup.addMatcherGroup(matcherGroup);
        }
        
        this.applicationAgentList = applicationAgentList;
    }
    
    public SortedMap<String, List<AgentInfoBo>> getapplicationAgentList() {
        return this.applicationAgentList;
    }
    
    public MatcherGroup getMatcherGroup() {
        return matcherGroup; 
    }
}
