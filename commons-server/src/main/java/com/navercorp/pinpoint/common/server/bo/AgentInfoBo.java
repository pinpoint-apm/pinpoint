/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.id.ApplicationId;
import jakarta.validation.constraints.NotBlank;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class AgentInfoBo {

    private final String hostName;
    private final String ip;
    private final String ports;
    @NotBlank private final String agentId;
    private final String agentName;
    @NotBlank private final String applicationName;
    private final ApplicationId applicationId;
    private final short serviceTypeCode;
    private final int pid;
    private final String vmVersion;
    private final String agentVersion;

    private final long startTime;

    private final long endTimeStamp;
    private final int endStatus;

    private final boolean container;

    // Should be serialized separately
    private final ServerMetaDataBo serverMetaData;
    private final JvmInfoBo jvmInfo;

    private AgentInfoBo(Builder builder) {
        this.hostName = builder.hostName;
        this.ip = builder.ip;
        this.ports = builder.ports;
        this.agentId = builder.agentId;
        this.agentName = builder.agentName;
        this.applicationName = builder.applicationName;
        this.applicationId = builder.applicationId;
        this.serviceTypeCode = builder.serviceTypeCode;
        this.pid = builder.pid;
        this.vmVersion = builder.vmVersion;
        this.agentVersion = builder.agentVersion;
        this.startTime = builder.startTime;
        this.endTimeStamp = builder.endTimeStamp;
        this.endStatus = builder.endStatus;
        this.container = builder.container;
        this.serverMetaData = builder.serverMetaData;
        this.jvmInfo = builder.jvmInfo;
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPorts() {
        return ports;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public int getEndStatus() {
        return endStatus;
    }

    public int getPid() {
        return pid;
    }

    public short getServiceTypeCode() {
        return serviceTypeCode;
    }


    public String getVmVersion() {
        return vmVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public boolean isContainer() {
        return container;
    }

    public ServerMetaDataBo getServerMetaData() {
        return this.serverMetaData;
    }

    public JvmInfoBo getJvmInfo() {
        return this.jvmInfo;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.getHostName());
        buffer.putPrefixedString(this.getIp());
        buffer.putPrefixedString(this.getPorts());
        buffer.putPrefixedString(this.getApplicationName());
        buffer.putShort(this.getServiceTypeCode());
        buffer.putInt(this.getPid());
        buffer.putPrefixedString(this.getAgentVersion());

        buffer.putLong(this.getStartTime());
        buffer.putLong(this.getEndTimeStamp());
        buffer.putInt(this.getEndStatus());

        buffer.putPrefixedString(this.getVmVersion());

        buffer.putBoolean(this.isContainer());
        buffer.putPrefixedString(this.getAgentName());

        buffer.putUUID(this.getApplicationId().value());

        return buffer.getBuffer();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentInfoBo other = (AgentInfoBo) obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AgentInfoBo{" +
                "hostName='" + hostName + '\'' +
                ", ip='" + ip + '\'' +
                ", ports='" + ports + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", serviceTypeCode=" + serviceTypeCode +
                ", pid=" + pid +
                ", vmVersion='" + vmVersion + '\'' +
                ", agentVersion='" + agentVersion + '\'' +
                ", startTime=" + startTime +
                ", endTimeStamp=" + endTimeStamp +
                ", endStatus=" + endStatus +
                ", container=" + container +
                ", serverMetaData=" + serverMetaData +
                ", jvmInfo=" + jvmInfo +
                '}';
    }

    public static class Builder {
        private String hostName;
        private String ip;
        private String ports;
        private String agentId;
        private String agentName;
        private String applicationName;
        private ApplicationId applicationId;
        private short serviceTypeCode;
        private int pid;
        private String vmVersion;
        private String agentVersion;

        private long startTime;
        private long endTimeStamp;
        private int endStatus;

        private boolean container;

        // Should be serialized separately
        private ServerMetaDataBo serverMetaData;
        private JvmInfoBo jvmInfo;

        public Builder() {
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPorts(String ports) {
            this.ports = ports;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public void setApplicationId(ApplicationId applicationId) {
            this.applicationId = applicationId;
        }

        public void setServiceTypeCode(short serviceTypeCode) {
            this.serviceTypeCode = serviceTypeCode;
        }

        public short getServiceTypeCode() {
            return serviceTypeCode;
        }

        public void setPid(int pid) {
            this.pid = pid;
        }

        public void setVmVersion(String vmVersion) {
            this.vmVersion = vmVersion;
        }

        public void setAgentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public void setEndTimeStamp(long endTimeStamp) {
            this.endTimeStamp = endTimeStamp;
        }

        public void setEndStatus(int endStatus) {
            this.endStatus = endStatus;
        }

        public void isContainer(boolean container) {
            this.container = container;
        }

        public void setServerMetaData(ServerMetaDataBo serverMetaData) {
            this.serverMetaData = serverMetaData;
        }

        public void setJvmInfo(JvmInfoBo jvmInfo) {
            this.jvmInfo = jvmInfo;
        }

        public AgentInfoBo build() {
            if (this.hostName == null)
                this.hostName = "";
            if (this.ip == null)
                this.ip = "";
            if (this.ports == null)
                this.ports = "";
            if (this.agentId == null)
                this.agentId = "";
            if (this.agentName == null)
                this.agentName = "";
            if (this.applicationName == null)
                this.applicationName = "";
            if (this.applicationId == null) {
                this.applicationId = ApplicationId.NOT_EXIST_APPLICATION_ID;
            }
            if (this.vmVersion == null)
                this.vmVersion = "";
            if (this.agentVersion == null) {
                this.agentVersion = "";
            }
            return new AgentInfoBo(this);
        }
    }
}
