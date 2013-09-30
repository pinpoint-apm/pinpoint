package com.nhn.pinpoint.web.service;

import java.util.List;
import java.util.SortedMap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * @author netspider
 */
public interface AgentInfoService {
	public SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(String applicationName, long from, long to);
}
