package com.navercorp.pinpoint.web.dao;

import java.util.List;

import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author hyungil.jeong
 */
public interface AgentStatDao {

    List<AgentStat> scanAgentStatList(String agentId, Range range);

}
