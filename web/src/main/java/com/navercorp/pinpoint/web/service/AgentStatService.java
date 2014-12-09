package com.navercorp.pinpoint.web.service;

import java.util.List;

import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author hyungil.jeong
 */
public interface AgentStatService {

    /**
     * 주어진 시간 범위에 따라 특정 agentId에 해당하는 시스템 통계 정보를 조회한다.
     * @param agentId
     * @param range
     * @return
     */
    List<AgentStat> selectAgentStatList(String agentId, Range range);

}
