package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import java.util.Objects;

public class AgentInfoFactory {
    private final ServiceTypeRegistryService registryService;

    public AgentInfoFactory(ServiceTypeRegistryService registryService) {
        this.registryService = Objects.requireNonNull(registryService, "registryService");
    }

    public AgentInfo build(AgentInfoBo agentInfoBo) {
        Objects.requireNonNull(agentInfoBo, "agentInfoBo");

        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(agentInfoBo.getApplicationName());
        agentInfo.setAgentId(agentInfoBo.getAgentId());
        agentInfo.setAgentName(agentInfoBo.getAgentName());
        agentInfo.setStartTimestamp(agentInfoBo.getStartTime());
        agentInfo.setHostName(agentInfoBo.getHostName());
        agentInfo.setIp(agentInfoBo.getIp());
        agentInfo.setPorts(agentInfoBo.getPorts());
        agentInfo.setServiceType(registryService.findServiceType(agentInfoBo.getServiceTypeCode()));
        agentInfo.setPid(agentInfoBo.getPid());
        agentInfo.setVmVersion(agentInfoBo.getVmVersion());
        agentInfo.setAgentVersion(agentInfoBo.getAgentVersion());
        agentInfo.setContainer(agentInfoBo.isContainer());
        return agentInfo;
    }
}
