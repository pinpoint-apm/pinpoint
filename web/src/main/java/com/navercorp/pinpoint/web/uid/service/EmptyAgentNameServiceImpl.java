package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnMissingBean(AgentNameService.class)
public class EmptyAgentNameServiceImpl implements AgentNameService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public EmptyAgentNameServiceImpl() {
        logger.info("EmptyAgentNameServiceImpl initialized");
    }
    
    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid) {
        return Collections.emptyList();
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return Collections.emptyList();
    }

    @Override
    public List<AgentIdentifier> getAgentIdentifier(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        return Collections.emptyList();
    }

    @Override
    public void deleteAllAgents(ServiceUid serviceUid, ApplicationUid applicationUid) {

    }

    @Override
    public void deleteAgent(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {

    }
}
