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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.web.view.AgentInfoSerializer;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentInfoSerializer.class)
public class AgentInfo {

    public static final Comparator<AgentInfo> AGENT_NAME_ASC_COMPARATOR = new Comparator<AgentInfo>() {
        @Override
        public int compare(AgentInfo lhs, AgentInfo rhs) {
            final String lhsAgentId = lhs.agentId == null ? "" : lhs.agentId;
            final String rhsAgentId = rhs.agentId == null ? "" : rhs.agentId;
            return lhsAgentId.compareTo(rhsAgentId);
        }
    };

    private String applicationName;
    private String agentId;
    private long startTimestamp;
    private String hostName;
    private String ip;
    private String ports;
    private short serviceTypeCode;
    private int pid;
    private String vmVersion;
    private String agentVersion;
    private ServerMetaDataBo serverMetaData;
    private JvmInfoBo jvmInfo;
    private long initialStartTimestamp;
    private AgentStatus status;

    public AgentInfo() {
    }

    public AgentInfo(AgentInfoBo agentInfoBo) {
        this.applicationName = agentInfoBo.getApplicationName();
        this.agentId = agentInfoBo.getAgentId();
        this.startTimestamp = agentInfoBo.getStartTime();
        this.hostName = agentInfoBo.getHostName();
        this.ip = agentInfoBo.getIp();
        this.ports = agentInfoBo.getPorts();
        this.serviceTypeCode = agentInfoBo.getServiceTypeCode();
        this.pid = agentInfoBo.getPid();
        this.vmVersion = agentInfoBo.getVmVersion();
        this.agentVersion = agentInfoBo.getAgentVersion();
        this.serverMetaData = agentInfoBo.getServerMetaData();
        this.jvmInfo = agentInfoBo.getJvmInfo();
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
        return serviceTypeCode;
    }

    public void setServiceTypeCode(short serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
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

    public ServerMetaDataBo getServerMetaData() {
        return serverMetaData;
    }

    public void setServerMetaData(ServerMetaDataBo serverMetaData) {
        this.serverMetaData = serverMetaData;
    }

    public JvmInfoBo getJvmInfo() {
        return jvmInfo;
    }

    public void setJvmInfo(JvmInfoBo jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    public long getInitialStartTimestamp() {
        return initialStartTimestamp;
    }

    public void setInitialStartTimestamp(long initialStartTimestamp) {
        this.initialStartTimestamp = initialStartTimestamp;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
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
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", hostName='").append(hostName).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", ports='").append(ports).append('\'');
        sb.append(", serviceTypeCode=").append(serviceTypeCode);
        sb.append(", pid=").append(pid);
        sb.append(", vmVersion='").append(vmVersion).append('\'');
        sb.append(", agentVersion='").append(agentVersion).append('\'');
        sb.append(", jvmInfo=").append(jvmInfo);
        sb.append(", initialStartTimestamp=").append(initialStartTimestamp);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
