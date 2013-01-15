package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.server.dao.AgentInfoDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.dto.thrift.Span;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.ApplicationIndexDao;

public class AgentInfoHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

    @Autowired
    private AgentInfoDao agentInfoDao;

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        if (!(tbase instanceof AgentInfo)) {
            logger.warn("invalid tbase:{}", tbase);
            return;
        }

        try {
            AgentInfo agentInfo = (AgentInfo) tbase;

            logger.debug("Received AgentInfo={}", agentInfo);

            agentInfoDao.insert(agentInfo);
            applicationIndexDao.insert(agentInfo);
            agentIdApplicationIndexDao.insert(agentInfo.getAgentId(), agentInfo.getApplicationName());
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
