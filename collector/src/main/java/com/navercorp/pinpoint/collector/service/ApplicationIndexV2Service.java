package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Supplier;

@Service
public class ApplicationIndexV2Service {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final boolean v2enabled;

    public ApplicationIndexV2Service(ApplicationDao applicationDao,
                                     AgentIdDao agentIdDao,
                                     @Value("${pinpoint.collector.application.index.v2.enabled:false}") boolean v2enabled) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "ApplicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.v2enabled = v2enabled;
    }

    // TODO get serviceUid from agentInfoBo
    public void insert(Supplier<ServiceUid> serviceUidSupplier, AgentInfoBo agentInfoBo) {
        if (!v2enabled) {
            return;
        }
        try {
            ServiceUid serviceUid = serviceUidSupplier.get();
            applicationDao.insert(serviceUid.getUid(), agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode());
            agentIdDao.insert(serviceUid.getUid(), agentInfoBo);
        } catch (Exception e) {
            logger.warn("Failed to insert agent. applicationName: {}, agentId: {}", agentInfoBo.getApplicationName(), agentInfoBo.getAgentId(), e);
        }
    }
}
