package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;

import java.util.Objects;

public class AgentInfoFactory {
    private final ServiceTypeRegistryService registryService;
    private final ServiceModelResolver serviceModelResolver;

    public AgentInfoFactory(ServiceTypeRegistryService registryService, ServiceModelResolver serviceModelResolver) {
        this.registryService = Objects.requireNonNull(registryService, "registryService");
        this.serviceModelResolver = Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
    }

    public AgentInfo build(AgentInfoBo agentInfoBo) {
        Objects.requireNonNull(agentInfoBo, "agentInfoBo");

        AgentInfo agentInfo = new AgentInfo();
        final Service service = serviceModelResolver.getService(agentInfoBo.getServiceUid().getUid());
        agentInfo.setServiceName(service.getServiceName());
        agentInfo.setApplicationName(agentInfoBo.getApplicationName());
        agentInfo.setAgentId(agentInfoBo.getAgentId());

        final String agentName = agentInfoBo.getAgentName();
        agentInfo.setAgentName(agentName, agentInfoBo.getAgentId());

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
