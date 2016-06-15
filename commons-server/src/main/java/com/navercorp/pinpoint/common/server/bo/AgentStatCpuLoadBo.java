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
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

/**
 * @author hyungil.jeong
 */
// FIXME (2015.10) Legacy column for storing serialzied Bos separately.
@Deprecated
public class AgentStatCpuLoadBo {

    private final String agentId;
    private final long startTimestamp;
    private final long timestamp;
    private final double jvmCpuLoad;    // range is  1 >= X >=0,  ex) if 25%  then save 0.25
    private final double systemCpuLoad; // range is  1 >= X >=0,  ex) if 25%  then save 0.25

    private AgentStatCpuLoadBo(Builder builder) {
        this.agentId = builder.agentId;
        this.startTimestamp = builder.startTimestamp;
        this.timestamp = builder.timestamp;
        this.jvmCpuLoad = builder.jvmCpuLoad;
        this.systemCpuLoad = builder.systemCpuLoad;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.agentId);
        buffer.putLong(this.startTimestamp);
        buffer.putLong(this.timestamp);
        buffer.putDouble(this.jvmCpuLoad);
        buffer.putDouble(this.systemCpuLoad);
        return buffer.getBuffer();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AgentStatCpuLoadBo{");
        sb.append("agentId='").append(this.agentId).append('\'');
        sb.append(", startTimestamp=").append(this.startTimestamp);
        sb.append(", timestamp=").append(this.timestamp);
        sb.append(", jvmCpuLoad=").append(this.jvmCpuLoad);
        sb.append(", systemCpuLoad=").append(this.systemCpuLoad);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
        AgentStatCpuLoadBo other = (AgentStatCpuLoadBo) obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        if (startTimestamp != other.startTimestamp)
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }

    public static class Builder {
        private static final double UNSUPPORTED = -1.0D;
        private final String agentId;
        private final long startTimestamp;
        private final long timestamp;
        private double jvmCpuLoad = UNSUPPORTED;
        private double systemCpuLoad = UNSUPPORTED;

        public Builder(final byte[] value) {
            final Buffer buffer = new FixedBuffer(value);
            this.agentId = buffer.readPrefixedString();
            this.startTimestamp = buffer.readLong();
            this.timestamp = buffer.readLong();
            this.jvmCpuLoad = buffer.readDouble();
            this.systemCpuLoad = buffer.readDouble();
        }

        public Builder(String agentId, long startTimestamp, long timestamp) {
            this.agentId = agentId;
            this.startTimestamp = startTimestamp;
            this.timestamp = timestamp;
        }

        public void jvmCpuLoad(double jvmCpuLoad) {
            this.jvmCpuLoad = jvmCpuLoad;
        }

        public void systemCpuLoad(double systemCpuLoad) {
            this.systemCpuLoad = systemCpuLoad;
        }

        public AgentStatCpuLoadBo build() {
            return new AgentStatCpuLoadBo(this);
        }
    }
}
