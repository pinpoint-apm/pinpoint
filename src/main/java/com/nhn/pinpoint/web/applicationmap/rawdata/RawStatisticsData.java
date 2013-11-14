package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.service.NodeId;

public class RawStatisticsData {
	private final Set<TransactionFlowStatistics> rawData;

	public RawStatisticsData(Set<TransactionFlowStatistics> rawData) {
		this.rawData = Collections.unmodifiableSet(rawData);
	}

    public Set<TransactionFlowStatistics> getRawData() {
        return rawData;
    }

    public Map<NodeId, Set<AgentInfoBo>> getAgentMap() {
		final Map<NodeId, Set<AgentInfoBo>> agentMap = new HashMap<NodeId, Set<AgentInfoBo>>();
		for (TransactionFlowStatistics stat : rawData) {
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
		return "RawStatisticsData [rawData=" + rawData + "]";
	}
}
