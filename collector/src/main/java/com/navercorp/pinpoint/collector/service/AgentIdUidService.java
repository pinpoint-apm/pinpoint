package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.AgentIdService;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class AgentIdUidService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentIdService agentIdService;
    private final boolean uidAgentListEnable;

    public AgentIdUidService(@Nullable AgentIdService agentIdService,
                             @Value("${pinpoint.collector.uid.agent.list.enabled:false}") boolean uidAgentListEnable) {
        this.agentIdService = agentIdService;
        this.uidAgentListEnable = uidAgentListEnable;
    }

    public void insert(Supplier<ServiceUid> serviceUidSupplier, Supplier<ApplicationUid> applicationUidSupplier, AgentInfoBo agentInfoBo) {
        if (!uidAgentListEnable || agentIdService == null) {
            return;
        }
        try {
            ServiceUid serviceUid = serviceUidSupplier.get();
            ApplicationUid applicationUid = applicationUidSupplier.get();
            agentIdService.insert(serviceUid, applicationUid, agentInfoBo.getAgentId());
        } catch (Exception e) {
            logger.warn("Failed to insert uid agent list. applicationName: {}, agentId: {}", agentInfoBo.getApplicationName(), agentInfoBo.getAgentId(), e);
        }
    }
}
