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

package com.navercorp.pinpoint.common.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

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
    private final short serviceType;
    private final int pid;
    private final String version;

    private final long startTime;

    private final long endTimeStamp;
    private final int endStatus;

    // Should be serialized separately
    private final ServerMetaDataBo serverMetaData;

    private AgentInfoBo(Builder builder) {
        this.hostName = builder.hostName;
        this.ip = builder.ip;
        this.ports = builder.ports;
        this.agentId = builder.agentId;
        this.applicationName = builder.applicationName;
        this.serviceType = builder.serviceType;
        this.pid = builder.pid;
        this.version = builder.version;
        this.startTime = builder.startTime;
        this.endTimeStamp = builder.endTimeStamp;
        this.endStatus = builder.endStatus;
        this.serverMetaData = builder.serverMetaData;
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

    public short getServiceType() {
        return serviceType;
    }

    public String getVersion() {
        return version;
    }
    
    public ServerMetaDataBo getServerMetaData() {
        return this.serverMetaData;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.getHostName());
        buffer.putPrefixedString(this.getIp());
        buffer.putPrefixedString(this.getPorts());
        buffer.putPrefixedString(this.getApplicationName());
        buffer.put(this.getServiceType());
        buffer.put(this.getPid());
        buffer.putPrefixedString(this.getVersion());

        buffer.put(this.getStartTime());
        buffer.put(this.getEndTimeStamp());
        buffer.put(this.getEndStatus());

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
        sb.append(", serviceType=").append(serviceType);
        sb.append(", pid=").append(pid);
        sb.append(", version='").append(version).append('\'');
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
        private short serviceType;
        private int pid;
        private String version;

        private long startTime;
        private long endTimeStamp;
        private int endStatus;
        
        // Should be serialized separately
        private ServerMetaDataBo serverMetaData;

        public Builder() {
        }

        public Builder(final byte[] value) {
            final Buffer buffer = new FixedBuffer(value);
            this.hostName = buffer.readPrefixedString();
            this.ip = buffer.readPrefixedString();
            this.ports = buffer.readPrefixedString();
            this.applicationName = buffer.readPrefixedString();
            this.serviceType = buffer.readShort();
            this.pid = buffer.readInt();
            this.version = buffer.readPrefixedString();

            this.startTime = buffer.readLong();
            this.endTimeStamp = buffer.readLong();
            this.endStatus = buffer.readInt();
        }

        public void hostName(String hostName) {
            this.hostName = hostName;
        }

        public void ip(String ip) {
            this.ip = ip;
        }

        public void ports(String ports) {
            this.ports = ports;
        }

        public void agentId(String agentId) {
            this.agentId = agentId;
        }

        public void applicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public void serviceType(short serviceType) {
            this.serviceType = serviceType;
        }

        public void pid(int pid) {
            this.pid = pid;
        }

        public void version(String version) {
            this.version = version;
        }

        public void startTime(long startTime) {
            this.startTime = startTime;
        }

        public void endTimeStamp(long endTimeStamp) {
            this.endTimeStamp = endTimeStamp;
        }

        public void endStatus(int endStatus) {
            this.endStatus = endStatus;
        }
        
        public void serverMetaData(ServerMetaDataBo serverMetaData) {
            this.serverMetaData = serverMetaData;
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
            if (this.version == null)
                this.version = "";
            return new AgentInfoBo(this);
        }
    }
}
