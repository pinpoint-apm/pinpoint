/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter.vo;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.scatter.TraceIndexValue;

import java.util.Objects;

public class DotMetaData {
    private final Dot dot;
    private final String agentName;
    private final String remoteAddr;
    private final String rpc;
    private final String endpoint;
    private final long spanId;
    private final long startTime;

    public DotMetaData(Dot dot, String agentName, String remoteAddr, String rpc, String endpoint, long spanId, long startTime) {
        this.dot = Objects.requireNonNull(dot, "dot");
        this.agentName = agentName;
        this.remoteAddr = remoteAddr;
        this.rpc = rpc;
        this.endpoint = endpoint;
        this.spanId = spanId;
        this.startTime = startTime;
    }

    public Dot getDot() {
        return dot;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRpc() {
        return rpc;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getSpanId() {
        return spanId;
    }

    public static class Builder {
        private Dot dot;
        private String agentName;
        private String remoteAddr;
        private String rpc;
        private String endpoint;
        private long spanId;
        private long startTime;

        public Builder() {
        }

        public void setDot(Dot dot) {
            this.dot = Objects.requireNonNull(dot, "dot");
        }

        public Dot getDot() {
            return dot;
        }

        public void read(byte[] bytes, int offset, int length) {
            Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
            read(buffer);
        }

        public void read(Buffer buffer) {
            // offset
            buffer.readByte();
            this.setSpanId(buffer.readLong());
            this.setStartTime(buffer.readLong());
            this.setRpc(buffer.readPrefixedString());
            this.setRemoteAddr(buffer.readPrefixedString());
            this.setEndpoint(buffer.readPrefixedString());
            this.setAgentName(buffer.readPrefixedString());
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public void setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
        }

        public void setRpc(String rpc) {
            this.rpc = rpc;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setSpanId(long spanId) {
            this.spanId = spanId;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public DotMetaData build() {
            return new DotMetaData(dot, agentName, remoteAddr, rpc, endpoint, spanId, startTime);
        }
    }

    public static class BuilderV2 {

        // row
        private long acceptedTime;
        private long spanId;

        // index
        private TraceIndexValue.Index index;

        // meta, metaRpc(optional)
        private TraceIndexValue.Meta meta;
        private TraceIndexValue.MetaRpc metaRpc;

        public BuilderV2() {
        }

        public void readIndex(byte[] bytes, int offset, int length) {
            this.index = TraceIndexValue.Index.decode(bytes, offset, length);
        }

        public void readMeta(byte[] bytes, int offset, int length) {
            this.meta = TraceIndexValue.Meta.decode(bytes, offset, length);
        }

        public void readMetaRpc(byte[] bytes, int offset, int length) {
            this.metaRpc = TraceIndexValue.MetaRpc.decode(bytes, offset, length);
        }

        public int getElapsedTime() {
            return index.elapsed();
        }

        public String getAgentId() {
            return index.agentId();
        }

        public int getExceptionCode() {
            return index.errorCode();
        }

        public void setAcceptedTime(long acceptedTime) {
            this.acceptedTime = acceptedTime;
        }

        public void setSpanId(long spanId) {
            this.spanId = spanId;
        }

        public DotMetaData build() {
            Dot dot = new Dot(meta.serverTraceId(), acceptedTime, index.elapsed(), index.errorCode(), index.agentId());
            String rpc = metaRpc != null ? metaRpc.rpc() : null;
            return new DotMetaData(dot, meta.agentName(), meta.remoteAddr(), rpc, meta.endpoint(), spanId, meta.startTime());
        }
    }
}
