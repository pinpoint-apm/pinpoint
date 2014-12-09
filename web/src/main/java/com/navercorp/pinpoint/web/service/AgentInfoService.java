package com.navercorp.pinpoint.web.service;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author netspider
 */
public interface AgentInfoService {
	SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(String applicationName, Range range);


    Set<AgentInfoBo> selectAgent(String applicationId, Range range);
}
