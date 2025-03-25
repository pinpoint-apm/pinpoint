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

package com.navercorp.pinpoint.web.vo.agent;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.ServiceTypeDescView;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentInfo {

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
    private boolean container;

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

    public void setAgentName(String agentName, String agentId) {
        if (StringUtils.isEmpty(agentName)) {
            this.setAgentName(agentId);
        } else {
            this.setAgentName(agentName);
        }
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

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public short getServiceTypeCode() {
        return serviceType.getCode();
    }

    @JsonSerialize(using = ServiceTypeDescView.class)
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }


    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
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
        result = 31 * result + Long.hashCode(startTimestamp);
        return result;
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", hostName='" + hostName + '\'' +
                ", ip='" + ip + '\'' +
                ", ports='" + ports + '\'' +
                ", serviceType=" + serviceType +
                ", pid=" + pid +
                ", vmVersion='" + vmVersion + '\'' +
                ", agentVersion='" + agentVersion + '\'' +
                ", container=" + container +
                '}';
    }


}
