/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo;

import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.view.AgentInfoSerializer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentInfoSerializer.class)
public class AgentInfo {

    public static final Comparator<AgentInfo> AGENT_NAME_ASC_COMPARATOR
            = Comparator.comparing(agentInfo -> StringUtils.defaultString(agentInfo.agentId));

    private String applicationName;
    private String agentId;
    private String agentName;
    private long startTimestamp;
    private String hostName;
    private String ip;
    private String ports;
    private ServiceType serviceType;
    private int pid;
    private String vmVersion;
    private String agentVersion;
    private ServerMetaDataBo serverMetaData;
    private JvmInfoBo jvmInfo;
    private long initialStartTimestamp;
    private boolean container;
    private AgentStatus status;

    public AgentInfo() {
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPorts() {
        return ports;
    }


    public short getServiceTypeCode() {
        return serviceType.getCode();
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public int getPid() {
        return pid;
    }


    public String getVmVersion() {
        return vmVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }


    public ServerMetaDataBo getServerMetaData() {
        return serverMetaData;
    }


    public JvmInfoBo getJvmInfo() {
        return jvmInfo;
    }


    public long getInitialStartTimestamp() {
        return initialStartTimestamp;
    }


    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public static class Binder {
        private final ServiceTypeRegistryService registryService;

        public Binder(ServiceTypeRegistryService registryService) {
            this.registryService = Objects.requireNonNull(registryService, "registryService");
        }

        public AgentInfo bind(AgentInfoBo agentInfoBo) {
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.applicationName = agentInfoBo.getApplicationName();
            agentInfo.agentId = agentInfoBo.getAgentId();
            agentInfo.agentName = agentInfoBo.getAgentName();
            agentInfo.startTimestamp = agentInfoBo.getStartTime();
            agentInfo.hostName = agentInfoBo.getHostName();
            agentInfo.ip = agentInfoBo.getIp();
            agentInfo.ports = agentInfoBo.getPorts();
            agentInfo.serviceType = registryService.findServiceType(agentInfoBo.getServiceTypeCode());
            agentInfo.pid = agentInfoBo.getPid();
            agentInfo.vmVersion = agentInfoBo.getVmVersion();
            agentInfo.agentVersion = agentInfoBo.getAgentVersion();
            agentInfo.serverMetaData = agentInfoBo.getServerMetaData();
            agentInfo.jvmInfo = agentInfoBo.getJvmInfo();
            agentInfo.container = agentInfoBo.isContainer();
            return agentInfo;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentInfo agentInfo = (AgentInfo) o;

        if (startTimestamp != agentInfo.startTimestamp) return false;
        return agentId != null ? agentId.equals(agentInfo.agentId) : agentInfo.agentId == null;

    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentInfo{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", agentName='").append(agentName).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", hostName='").append(hostName).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", ports='").append(ports).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", pid=").append(pid);
        sb.append(", vmVersion='").append(vmVersion).append('\'');
        sb.append(", agentVersion='").append(agentVersion).append('\'');
        sb.append(", serverMetaData='").append(serverMetaData).append('\'');
        sb.append(", jvmInfo=").append(jvmInfo);
        sb.append(", initialStartTimestamp=").append(initialStartTimestamp);
        sb.append(", container=").append(container);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }



}
