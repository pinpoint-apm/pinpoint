package com.profiler.server.handler;

import java.net.DatagramPacket;

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
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        assert (tbase instanceof Span);

        try {
            AgentInfo agentInfo = (AgentInfo) tbase;

            logger.debug("Received AgentInfo={}", agentInfo);

            applicationIndexDao.insert(agentInfo);
            agentIdApplicationIndexDao.insert(agentInfo.getAgentId(), agentInfo.getApplicationName());
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
