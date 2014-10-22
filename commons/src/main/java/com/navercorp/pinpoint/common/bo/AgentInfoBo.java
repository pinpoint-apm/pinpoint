package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;

import java.util.Comparator;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class AgentInfoBo {

    public static final Comparator<AgentInfoBo> AGENT_NAME_ASC_COMPARATOR = new Comparator<AgentInfoBo>() {
        @Override
        public int compare(AgentInfoBo that, AgentInfoBo other) {
            // null 일때 상황이 애매할수 있어서 그냥 ""으로 처리함.
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
    private final ServiceType serviceType;
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

    public ServiceType getServiceType() {
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
        buffer.put(this.serviceType.getCode());
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
        private ServiceType serviceType;
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
            this.serviceType = ServiceType.findServiceType(buffer.readShort());
            this.pid = buffer.readInt();
            this.version = buffer.readPrefixedString();

            this.startTime = buffer.readLong();
            this.endTimeStamp = buffer.readLong();
            this.endStatus = buffer.readInt();
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder ports(String ports) {
            this.ports = ports;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder serviceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder pid(int pid) {
            this.pid = pid;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTimeStamp(long endTimeStamp) {
            this.endTimeStamp = endTimeStamp;
            return this;
        }

        public Builder endStatus(int endStatus) {
            this.endStatus = endStatus;
            return this;
        }
        
        public Builder serverMetaData(ServerMetaDataBo serverMetaData) {
            this.serverMetaData = serverMetaData;
            return this;
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
            if (this.serviceType == null)
                this.serviceType = ServiceType.UNKNOWN;
            return new AgentInfoBo(this);
        }
    }
}
