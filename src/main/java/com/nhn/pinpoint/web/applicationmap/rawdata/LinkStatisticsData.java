package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.service.NodeId;

public class LinkStatisticsData {
	private final Set<LinkStatistics> linkStatData;

	public LinkStatisticsData(Set<LinkStatistics> linkStatData) {
        if (linkStatData == null) {
            throw new NullPointerException("linkStatData must not be null");
        }
        this.linkStatData = linkStatData;
	}

    public Set<LinkStatistics> getLinkStatData() {
        return linkStatData;
    }

    public Map<NodeId, Set<AgentInfoBo>> getAgentMap() {
		final Map<NodeId, Set<AgentInfoBo>> agentMap = new HashMap<NodeId, Set<AgentInfoBo>>();
		for (LinkStatistics stat : linkStatData) {
			if (stat.getToAgentSet() == null) {
				continue;
			}
            NodeId key = stat.getToApplicationId();
            final Set<AgentInfoBo> agentInfoBos = agentMap.get(key);
            if (agentInfoBos != null) {
				Set<AgentInfoBo> toAgentSet = stat.getToAgentSet();
				if (toAgentSet != null) {
					agentInfoBos.addAll(stat.getToAgentSet());
				}
			} else {
				agentMap.put(key, stat.getToAgentSet());
			}
		}
		return agentMap;
	}

	@Override
	public String toString() {
		return "LinkStatisticsData [linkStatData=" + linkStatData + "]";
	}
}
