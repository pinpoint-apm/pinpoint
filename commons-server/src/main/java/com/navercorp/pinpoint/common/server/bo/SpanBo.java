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
 * @author emeroad
 */
public class SpanBo implements BasicSpan {

    private static final int UNDEFINED = ServiceType.UNDEFINED.getCode();
    public static final long DEFAULT_END_TIME = -1L;

    // version 0 means that the type of prefix's size is int
    private byte version = 0;

    @NonNull
    private final TraceSourceType traceSourceType;

    private final SpanOwner owner;

    private ServerTraceId transactionId;

    private long spanId;
    private long parentSpanId;

    private ParentApplication parentApplication;

    private long startTime;
    private long endTime = DEFAULT_END_TIME;
    private int elapsed;

    private String rpc;
    private int serviceType;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList = new ArrayList<>();
    private short flag; // optional
    private int errCode;

    private final List<SpanEventBo> spanEventBoList = new ArrayList<>();
    private List<SpanChunkBo> spanChunkBoList;

    private long collectorAcceptTime;

    private ExceptionInfo exceptionInfo;

    private String exceptionClass;

    private int applicationServiceType;

    private String acceptorHost;
    private String remoteAddr; // optional

    private byte loggingTransactionInfo; //optional

    private List<AttributeBo> attributeBoList = new ArrayList<>();

    public SpanBo() {
        this(TraceSourceType.PINPOINT, new SpanOwner());
    }

    public SpanBo(TraceSourceType traceSourceType) {
        this(traceSourceType, new SpanOwner());
    }

    public SpanBo(TraceSourceType traceSourceType, SpanOwner owner) {
        this.traceSourceType = Objects.requireNonNull(traceSourceType, "traceSourceType");
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public int getVersion() {
        return Byte.toUnsignedInt(version);
    }

    public byte getRawVersion() {
        return version;
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
    public ServerTraceId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(ServerTraceId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public SpanOwner getSpanOwner() {
        return owner;
    }

    @NonNull
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

    //------------

    @Override
    public long getAgentStartTime() {
        return owner.getAgentStartTime();
    }

    public long getStartTimeMillis() {
        if (getVersion() == SpanVersion.TRACE_V3) {
            return TimeUnit.NANOSECONDS.toMillis(startTime);
        }
        return startTime;
    }

    public long getStartTimeNanos() {
        if (getVersion() == SpanVersion.TRACE_V3) {
            return startTime;
        }
        return TimeUnit.MILLISECONDS.toNanos(startTime);
    }

    /**
     * Sets elapsed-only span time for pre-V3 data.
     * startTime is epoch millis and endTime stays unset because the persisted model stores
     * startTime + elapsedMillis.
     */
    public void setTraceTime(int version, long startTime, int elapsedMillis) {
        if (version == SpanVersion.TRACE_V3) {
            throw new IllegalArgumentException("TRACE_V3 span requires absolute start/end time");
        }

        setVersion(version);
        this.startTime = startTime;
        this.elapsed = elapsedMillis;
        this.endTime = DEFAULT_END_TIME;
    }

    /**
     * Sets absolute span time for TRACE_V3 data.
     * startTime/endTime are epoch nanos. elapsedMillis is retained as the compatibility duration.
     */
    public void setTraceTime(int version, long startTime, long endTime, int elapsedMillis) {
        if (version != SpanVersion.TRACE_V3) {
            throw new IllegalArgumentException("absolute start/end time is only supported for TRACE_V3 spans");
        }
        if (endTime == DEFAULT_END_TIME) {
            throw new IllegalArgumentException("TRACE_V3 span end time is required");
        }
        if (endTime < startTime) {
            throw new IllegalArgumentException("span end time must be greater than or equal to start time");
        }

        setVersion(version);
        this.startTime = startTime;
        this.elapsed = elapsedMillis;
        this.endTime = endTime;
    }

    public boolean hasEndTime() {
        return endTime != DEFAULT_END_TIME;
    }

    public long getEndTimeMillis() {
        if (hasEndTime()) {
            if (getVersion() == SpanVersion.TRACE_V3) {
                return TimeUnit.NANOSECONDS.toMillis(endTime);
            }
            return endTime;
        }
        return getStartTimeMillis() + elapsed;
    }

    public long getEndTimeNanos() {
        if (hasEndTime()) {
            if (getVersion() == SpanVersion.TRACE_V3) {
                return endTime;
            }
            return TimeUnit.MILLISECONDS.toNanos(endTime);
        }
        return getStartTimeNanos() + TimeUnit.MILLISECONDS.toNanos(elapsed);
    }

    /**
     * Returns the span elapsed time in milliseconds.
     */
    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }


    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }

    @Override
    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    public void addAnnotation(AnnotationBo annotationBo) {
        if (annotationBo == null) {
            return;
        }
        this.annotationBoList.add(annotationBo);
    }

    public void setAnnotationBoList(List<AnnotationBo> anoList) {
        if (anoList == null) {
            return;
        }
        this.annotationBoList = anoList;
    }

    public void addSpanEventBoList(List<SpanEventBo> spanEventBoList) {
        if (spanEventBoList == null) {
            return;
        }
        this.spanEventBoList.addAll(spanEventBoList);
    }


    public void addSpanEvent(SpanEventBo spanEventBo) {
        if (spanEventBo == null) {
            return;
        }
        spanEventBoList.add(spanEventBo);
    }

    public List<SpanEventBo> getSpanEventBoList() {
        return spanEventBoList;
    }

    public List<SpanChunkBo> getSpanChunkBoList() {
        if (spanChunkBoList == null) {
            spanChunkBoList = new ArrayList<>();
        }
        return spanChunkBoList;
    }

    public void addSpanChunkBo(SpanChunkBo asyncSpanBo) {
        if (spanChunkBoList == null) {
            this.spanChunkBoList = new ArrayList<>();
        }
        this.spanChunkBoList.add(asyncSpanBo);
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public boolean hasError() {
        return errCode > 0;
    }

    public String getAcceptorHost() {
        return acceptorHost;
    }

    public void setAcceptorHost(String acceptorHost) {
        this.acceptorHost = acceptorHost;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    @Override
    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public boolean isRoot() {
        return -1L == parentSpanId;
    }

    public ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }

    public boolean hasException() {
        return exceptionInfo != null;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    @Override
    public void setApplicationServiceType(int applicationServiceType) {
        this.applicationServiceType = applicationServiceType;
    }

    @Override
    public boolean hasApplicationServiceType() {
        return applicationServiceType != 0 && applicationServiceType != UNDEFINED;
    }

    @Override
    public int getApplicationServiceType() {
        if (hasApplicationServiceType()) {
            return this.applicationServiceType;
        } else {
            return this.serviceType;
        }
    }


    public ParentApplication getParentApplication() {
        return parentApplication;
    }

    public void setParentApplication(ParentApplication parentApplication) {
        this.parentApplication = parentApplication;
    }

    /**
     * @return loggingInfo key
     * @see com.navercorp.pinpoint.common.trace.LoggingInfo
     */
    public byte getLoggingTransactionInfo() {
        return loggingTransactionInfo;
    }


    public void setLoggingTransactionInfo(byte loggingTransactionInfo) {
        this.loggingTransactionInfo = loggingTransactionInfo;
    }

    public List<AttributeBo> getAttributeBoList() {
        return attributeBoList;
    }

    public void setAttributeBoList(List<AttributeBo> attributeBoList) {
        if (attributeBoList == null) {
            return;
        }
        this.attributeBoList = attributeBoList;
    }

    public void addAttribute(AttributeBo attributeBo) {
        if (attributeBo == null) {
            return;
        }
        if (this.attributeBoList == null) {
            this.attributeBoList = new ArrayList<>();
        }
        this.attributeBoList.add(attributeBo);
    }

    @Override
    public String toString() {
        return "SpanBo{" +
                "version=" + version +
                ", traceSourceType=" + traceSourceType +
                ", owner=" + owner +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", parentApplication=" + parentApplication +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", elapsed=" + elapsed +
                ", rpc='" + rpc + '\'' +
                ", serviceType=" + serviceType +
                ", endPoint='" + endPoint + '\'' +
                ", apiId=" + apiId +
                ", annotationBoList=" + annotationBoList +
                ", flag=" + flag +
                ", errCode=" + errCode +
                ", spanEventBoList=" + spanEventBoList +
                ", spanChunkBoList=" + spanChunkBoList +
                ", collectorAcceptTime=" + collectorAcceptTime +
                ", exceptionInfo=" + exceptionInfo +
                ", exceptionClass='" + exceptionClass + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", acceptorHost='" + acceptorHost + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", loggingTransactionInfo=" + loggingTransactionInfo +
                ", attributeBoList=" + attributeBoList +
                '}';
    }

}
