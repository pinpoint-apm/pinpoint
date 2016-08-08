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

import java.util.Comparator;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class AgentInfoBo {

    public static final Comparator<AgentInfoBo> AGENT_NAME_ASC_COMPARATOR = new Comparator<AgentInfoBo>() {
        @Override
        public int compare(AgentInfoBo that, AgentInfoBo other) {
            final String thatAgentId = defaultString(that.agentId);
            final String otherAgentId = defaultString(other.agentId);
            return thatAgentId.compareTo(otherAgentId);
        }

        private String defaultString(String string) {
            return string == null ? "" : string;
        }
    };

    private final String hostName;
    private final String ip;
    private final String ports;
    private final String agentId;
    private final String applicationName;
    private final short serviceTypeCode;
    private final int pid;
    private final String vmVersion;
    private final String agentVersion;

    private final long startTime;

    private final long endTimeStamp;
    private final int endStatus;

    // Should be serialized separately
    private final ServerMetaDataBo serverMetaData;
    private final JvmInfoBo jvmInfo;

    private AgentInfoBo(Builder builder) {
        this.hostName = builder.hostName;
        this.ip = builder.ip;
        this.ports = builder.ports;
        this.agentId = builder.agentId;
        this.applicationName = builder.applicationName;
        this.serviceTypeCode = builder.serviceTypeCode;
        this.pid = builder.pid;
        this.vmVersion = builder.vmVersion;
        this.agentVersion = builder.agentVersion;
        this.startTime = builder.startTime;
        this.endTimeStamp = builder.endTimeStamp;
        this.endStatus = builder.endStatus;
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

    public String getApplicationName() {
        return applicationName;
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
        AgentInfoBo other = (AgentInfoBo)obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentInfoBo{");
        sb.append("hostName='").append(hostName).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", ports='").append(ports).append('\'');
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", serviceTypeCode=").append(serviceTypeCode);
        sb.append(", pid=").append(pid);
        sb.append(", vmVersion=").append(vmVersion).append('\'');
        sb.append(", agentVersion='").append(agentVersion).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", endTimeStamp=").append(endTimeStamp);
        sb.append(", endStatus=").append(endStatus);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String hostName;
        private String ip;
        private String ports;
        private String agentId;
        private String applicationName;
        private short serviceTypeCode;
        private int pid;
        private String vmVersion;
        private String agentVersion;

        private long startTime;
        private long endTimeStamp;
        private int endStatus;
        
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

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
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
            if (this.applicationName == null)
                this.applicationName = "";
            if (this.vmVersion == null)
                this.vmVersion = "";
            if (this.agentVersion == null) {
                this.agentVersion = "";
            }
            return new AgentInfoBo(this);
        }
    }
}
