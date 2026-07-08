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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ByteUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.SpanVersion;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkBo implements BasicSpan {

    private static final int UNDEFINED = ServiceType.UNDEFINED.getCode();

    private byte version = 0;
    @NonNull
    private final TraceSourceType traceSourceType;
    private final SpanOwner owner;

    private ServerTraceId transactionId;

    private long spanId;
    private String endPoint;

    private int applicationServiceType;

    private final List<SpanEventBo> spanEventBoList = new ArrayList<>();

    private long collectorAcceptTime;

    private LocalAsyncIdBo localAsyncId;
    private long keyTime;


    public SpanChunkBo() {
        this(TraceSourceType.PINPOINT, new SpanOwner());
    }

    public SpanChunkBo(TraceSourceType traceSourceType) {
        this(traceSourceType, new SpanOwner());
    }

    public SpanChunkBo(TraceSourceType traceSourceType, SpanOwner owner) {
        this.traceSourceType = Objects.requireNonNull(traceSourceType, "traceSourceType");
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public int getVersion() {
        return Byte.toUnsignedInt(version);
    }

    public void setVersion(int version) {
        this.version = ByteUtils.toUnsignedByte(version);
    }

    @NonNull
    @Override
    public TraceSourceType getTraceSourceType() {
        return traceSourceType;
    }

    @Override
    public SpanOwner getSpanOwner() {
        return owner;
    }

    @Override
    public String getAgentId() {
        return owner.getAgentId();
    }

    @Override
    public String getAgentName() {
        return owner.getAgentName();
    }

    @NonNull
    @Override
    public String getApplicationName() {
        return owner.getApplicationName();
    }

    @NonNull
    @Override
    public String getServiceName() {
        return owner.getServiceName();
    }

    @Override
    public ServiceUid getServiceUid() {
        return owner.getServiceUid();
    }

    @Override
    public long getAgentStartTime() {
        return owner.getAgentStartTime();
    }

    @Override
    public ServerTraceId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(ServerTraceId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }

    @Override
    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getKeyTimeMillis() {
        if (getVersion() == SpanVersion.TRACE_V3) {
            return TimeUnit.NANOSECONDS.toMillis(keyTime);
        }
        return keyTime;
    }

    public long getKeyTimeNanos() {
        if (getVersion() == SpanVersion.TRACE_V3) {
            return keyTime;
        }
        return TimeUnit.MILLISECONDS.toNanos(keyTime);
    }

    /**
     * Sets the span chunk time fields as one versioned unit.
     * TRACE_V2 uses epoch millis. TRACE_V3 uses epoch nanos.
     */
    public void setTraceTime(int version, long keyTime) {
        if (version != SpanVersion.TRACE_V2 && version != SpanVersion.TRACE_V3) {
            throw new IllegalArgumentException("span chunk keyTime is only supported for TRACE_V2 or TRACE_V3");
        }
        setVersion(version);
        this.keyTime = keyTime;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    @Override
    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    @Override
    public void setApplicationServiceType(int applicationServiceType) {
        this.applicationServiceType  = applicationServiceType;
    }

    @Override
    public int getApplicationServiceType() {
        return this.applicationServiceType;
    }

    @Override
    public boolean hasApplicationServiceType() {
        return applicationServiceType != 0 && applicationServiceType != UNDEFINED;
    }

    public List<SpanEventBo> getSpanEventBoList() {
        return spanEventBoList;
    }

    public void addSpanEventBoList(List<SpanEventBo> spanEventBoList) {
        if (spanEventBoList == null) {
            return;
        }
        this.spanEventBoList.addAll(spanEventBoList);
    }

    public void addSpanEvent(SpanEventBo spanEvent) {
        if (spanEvent == null) {
            return;
        }
        this.spanEventBoList.add(spanEvent);
    }

    public boolean isAsyncSpanChunk() {
        return localAsyncId != null;
    }

    public LocalAsyncIdBo getLocalAsyncId() {
        return localAsyncId;
    }

    public void setLocalAsyncId(LocalAsyncIdBo localAsyncId) {
        this.localAsyncId = localAsyncId;
    }

    @Override
    public String toString() {
        return "SpanChunkBo{" +
                "version=" + version +
                ", traceSourceType=" + traceSourceType +
                ", owner=" + owner +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", endPoint='" + endPoint + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", spanEventBoList=" + spanEventBoList +
                ", collectorAcceptTime=" + collectorAcceptTime +
                ", localAsyncId=" + localAsyncId +
                ", keyTime=" + keyTime +
                '}';
    }
}
