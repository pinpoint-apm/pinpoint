package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

public class RawStatisticsData implements Iterable<TransactionFlowStatistics> {
	private final Set<TransactionFlowStatistics> rawData;

	public RawStatisticsData(Set<TransactionFlowStatistics> rawData) {
		this.rawData = Collections.unmodifiableSet(rawData);
	}

	@Override
	public Iterator<TransactionFlowStatistics> iterator() {
		return rawData.iterator();
	}

	public Map<String, Set<AgentInfoBo>> getAgentMap() {
		Map<String, Set<AgentInfoBo>> agentMap = new HashMap<String, Set<AgentInfoBo>>();
		for (TransactionFlowStatistics stat : rawData) {
			if (stat.getToAgentSet() == null) {
				continue;
			}
			String key = stat.getTo() + stat.getToServiceType();
			if (agentMap.containsKey(key)) {
				Set<AgentInfoBo> toAgentSet = stat.getToAgentSet();
				if (toAgentSet != null) {
					agentMap.get(key).addAll(stat.getToAgentSet());
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
