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
public class AgentStatMemoryGcBo {

    private final String agentId;
    private final long startTimestamp;
    private final long timestamp;
    private final String gcType;
    private final long jvmMemoryHeapUsed;
    private final long jvmMemoryHeapMax;
    private final long jvmMemoryNonHeapUsed;
    private final long jvmMemoryNonHeapMax;
    private final long jvmGcOldCount;
    private final long jvmGcOldTime;

    private AgentStatMemoryGcBo(Builder builder) {
        this.agentId = builder.agentId;
        this.startTimestamp = builder.startTimestamp;
        this.timestamp = builder.timestamp;
        this.gcType = builder.gcType;
        this.jvmMemoryHeapUsed = builder.jvmMemoryHeapUsed;
        this.jvmMemoryHeapMax = builder.jvmMemoryHeapMax;
        this.jvmMemoryNonHeapUsed = builder.jvmMemoryNonHeapUsed;
        this.jvmMemoryNonHeapMax = builder.jvmMemoryNonHeapMax;
        this.jvmGcOldCount = builder.jvmGcOldCount;
        this.jvmGcOldTime = builder.jvmGcOldTime;
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

    public String getGcType() {
        return gcType;
    }

    public long getJvmMemoryHeapUsed() {
        return jvmMemoryHeapUsed;
    }

    public long getJvmMemoryHeapMax() {
        return jvmMemoryHeapMax;
    }

    public long getJvmMemoryNonHeapUsed() {
        return jvmMemoryNonHeapUsed;
    }

    public long getJvmMemoryNonHeapMax() {
        return jvmMemoryNonHeapMax;
    }

    public long getJvmGcOldCount() {
        return jvmGcOldCount;
    }

    public long getJvmGcOldTime() {
        return jvmGcOldTime;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(this.agentId);
        buffer.putLong(this.startTimestamp);
        buffer.putLong(this.timestamp);
        buffer.putPrefixedString(this.gcType);
        buffer.putLong(this.jvmMemoryHeapUsed);
        buffer.putLong(this.jvmMemoryHeapMax);
        buffer.putLong(this.jvmMemoryNonHeapUsed);
        buffer.putLong(this.jvmMemoryNonHeapMax);
        buffer.putLong(this.jvmGcOldCount);
        buffer.putLong(this.jvmGcOldTime);
        return buffer.getBuffer();
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
        AgentStatMemoryGcBo other = (AgentStatMemoryGcBo) obj;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AgentStatMemoryGcBo{");
        sb.append("agentId='").append(this.agentId).append('\'');
        sb.append(", startTimestamp=").append(this.startTimestamp);
        sb.append(", timestamp=").append(this.timestamp);
        sb.append(", gcType='").append(this.gcType).append('\'');
        sb.append(", jvmMemoryHeapUsed=").append(this.jvmMemoryHeapUsed);
        sb.append(", jvmMemoryHeapMax=").append(this.jvmMemoryHeapMax);
        sb.append(", jvmMemoryNonHeapUsed=").append(this.jvmMemoryNonHeapUsed);
        sb.append(", jvmMemoryNonHeapMax=").append(this.jvmMemoryNonHeapMax);
        sb.append(", jvmGcOldCount=").append(this.jvmGcOldCount);
        sb.append(", jvmGcOldTime=").append(this.jvmGcOldTime);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private final String agentId;
        private final long startTimestamp;
        private final long timestamp;
        private String gcType;
        private long jvmMemoryHeapUsed;
        private long jvmMemoryHeapMax;
        private long jvmMemoryNonHeapUsed;
        private long jvmMemoryNonHeapMax;
        private long jvmGcOldCount;
        private long jvmGcOldTime;

        public Builder(final byte[] value) {
            final Buffer buffer = new FixedBuffer(value);
            this.agentId = buffer.readPrefixedString();
            this.startTimestamp = buffer.readLong();
            this.timestamp = buffer.readLong();
            this.gcType = buffer.readPrefixedString();
            this.jvmMemoryHeapUsed = buffer.readLong();
            this.jvmMemoryHeapMax = buffer.readLong();
            this.jvmMemoryNonHeapUsed = buffer.readLong();
            this.jvmMemoryNonHeapMax = buffer.readLong();
            this.jvmGcOldCount = buffer.readLong();
            this.jvmGcOldTime = buffer.readLong();
        }

        public Builder(String agentId, long startTimestamp, long timestamp) {
            this.agentId = agentId;
            this.startTimestamp = startTimestamp;
            this.timestamp = timestamp;
        }

        public void gcType(String gcType) {
            this.gcType = gcType;
        }

        public void jvmMemoryHeapUsed(long jvmMemoryHeapUsed) {
            this.jvmMemoryHeapUsed = jvmMemoryHeapUsed;
        }

        public void jvmMemoryHeapMax(long jvmMemoryHeapMax) {
            this.jvmMemoryHeapMax = jvmMemoryHeapMax;
        }

        public void jvmMemoryNonHeapUsed(long jvmMemoryNonHeapUsed) {
            this.jvmMemoryNonHeapUsed = jvmMemoryNonHeapUsed;
        }

        public void jvmMemoryNonHeapMax(long jvmMemoryNonHeapMax) {
            this.jvmMemoryNonHeapMax = jvmMemoryNonHeapMax;
        }

        public void jvmGcOldCount(long jvmGcOldCount) {
            this.jvmGcOldCount = jvmGcOldCount;
        }

        public void jvmGcOldTime(long jvmGcOldTime) {
            this.jvmGcOldTime = jvmGcOldTime;
        }

        public AgentStatMemoryGcBo build() {
            return new AgentStatMemoryGcBo(this);
        }
    }
}
