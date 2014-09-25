package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Range;

/**
 * @author hyungil.jeong
 */
public interface AgentStatDao {

    List<AgentStat> scanAgentStatList(String agentId, Range range);

}
