package com.nhn.pinpoint.web.service;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.vo.Range;

/**
 * @author netspider
 */
public interface AgentInfoService {
	SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(String applicationName, Range range);


    Set<AgentInfoBo> selectAgent(String applicationId);
}
