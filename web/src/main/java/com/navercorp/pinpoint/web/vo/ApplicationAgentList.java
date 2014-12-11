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
