package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.trace.ServiceType;
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
    private final CachedApplicationServiceTypeService cachedApplicationServiceTypeService;
    private final boolean v2enabled;
    private final Set<Integer> missingHeaderServiceTypeCodes;

    public ApplicationIndexV2Service(ApplicationDao applicationDao,
                                     AgentIdDao agentIdDao,
                                     CachedApplicationServiceTypeService cachedApplicationServiceTypeService,
                                     AgentProperties agentProperties,
                                     @Value("${pinpoint.collector.application.index.v2.enabled:false}") boolean v2enabled) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "ApplicationDao");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.cachedApplicationServiceTypeService = Objects.requireNonNull(cachedApplicationServiceTypeService, "applicationServiceTypeService");
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
            if (headerServiceTypeCode == ServiceType.UNDEFINED.getCode()) {
                handleMissingHeaderServiceType(serviceUid, agentInfoBo);
            }

            agentIdDao.insert(serviceUid.getUid(), agentInfoBo);
            applicationDao.insert(serviceUid.getUid(), agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode());
        } catch (Exception e) {
            logger.warn("Failed to insert agent. applicationName: {}, agentId: {}", agentInfoBo.getApplicationName(), agentInfoBo.getAgentId(), e);
        }
    }

    private void handleMissingHeaderServiceType(ServiceUid serviceUid, AgentInfoBo agentInfoBo) {
        if (serviceUid.equals(ServiceUid.DEFAULT)) {
            // Cache serviceType for ping session resolution
            cachedApplicationServiceTypeService.put(agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode());
            if (!missingHeaderServiceTypeCodes.contains(agentInfoBo.getServiceTypeCode())) {
                logger.warn("Unhandled missing header serviceType. agentServiceType={}, agentId={}", agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
            }
        } else {
            logger.warn("Missing serviceType header for non-default service. serviceUid={}, agentServiceType={}, agentId={}", serviceUid, agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
        }
        // Handle missing serviceType: initial pings cannot resolve serviceType,
        // so update agent state here when agentInfo arrives with the actual serviceType
        //agentIdDao.updateState(serviceUid.getUid(), agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId(), agentInfoBo.getStartTime(), agentInfoBo.getStartTime(), AgentLifeCycleState.RUNNING);
    }
}
