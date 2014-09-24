package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Range;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.dao.AgentStatDao;

/**
 * @author harebox
 * @author hyungil.jeong
 */
@Service
public class AgentStatServiceImpl implements AgentStatService {

    @Autowired
    private AgentStatDao agentStatDao;

    public List<AgentStat> selectAgentStatList(String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        return agentStatDao.scanAgentStatList(agentId, range);
    }

}
