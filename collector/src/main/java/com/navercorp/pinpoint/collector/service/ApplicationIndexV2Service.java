package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Service
public class ApplicationIndexV2Service {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDao applicationDao;
    private final AgentIdDao agentIdDao;
    private final boolean v2enabled;
    private final Set<Integer> missingHeaderServiceTypeCodes;

    public ApplicationIndexV2Service(ApplicationDao applicationDao,
                                     AgentIdDao agentIdDao,
                                     AgentProperties agentProperties,
                                     @Value("${pinpoint.collector.application.index.v2.enabled:false}") boolean v2enabled) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "ApplicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.v2enabled = v2enabled;
        this.missingHeaderServiceTypeCodes = agentProperties.getMissingHeaderServiceTypeCodes();
    }

    // TODO get serviceUid from agentInfoBo
    public void insert(Supplier<ServiceUid> serviceUidSupplier, int headerServiceTypeCode, AgentInfoBo agentInfoBo) {
        if (!v2enabled) {
            return;
        }
        try {
            ServiceUid serviceUid = serviceUidSupplier.get();
            applicationDao.insert(serviceUid.getUid(), agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode());
            agentIdDao.insert(serviceUid.getUid(), headerServiceTypeCode, agentInfoBo);

            if (headerServiceTypeCode != agentInfoBo.getServiceTypeCode()) {
                if (headerServiceTypeCode == -1 && missingHeaderServiceTypeCodes.contains(agentInfoBo.getServiceTypeCode())) {
                    logger.debug("Known missing header serviceType. agentServiceType={}, agentId={}", agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
                } else if (headerServiceTypeCode == -1) {
                    logger.warn("Unhandled missing header serviceType. agentServiceType={}, agentId={}", agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
                } else {
                    logger.warn("Header serviceType mismatch. headerServiceType={}, agentServiceType={}, agentId={}", headerServiceTypeCode, agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to insert agent. applicationName: {}, agentId: {}", agentInfoBo.getApplicationName(), agentInfoBo.getAgentId(), e);
        }
    }
}
