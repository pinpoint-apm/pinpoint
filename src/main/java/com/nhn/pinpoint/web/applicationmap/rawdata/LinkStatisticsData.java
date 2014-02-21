package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.vo.Application;

public class LinkStatisticsData {

	private final Collection<LinkStatistics> linkStatData;

	public LinkStatisticsData(Collection<LinkStatistics> linkStatData) {
        if (linkStatData == null) {
            throw new NullPointerException("linkStatData must not be null");
        }
        this.linkStatData = linkStatData;
	}

    public Collection<LinkStatistics> getLinkStatData() {
        return linkStatData;
    }

    public Map<Application, Set<AgentInfoBo>> getAgentMap() {
		final Map<Application, Set<AgentInfoBo>> agentMap = new HashMap<Application, Set<AgentInfoBo>>();
		for (LinkStatistics stat : linkStatData) {
			if (stat.getToAgentSet() == null) {
				continue;
			}
            Application key = stat.getToApplication();
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
