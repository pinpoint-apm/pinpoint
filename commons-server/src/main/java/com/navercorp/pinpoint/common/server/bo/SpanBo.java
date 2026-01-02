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
import com.navercorp.pinpoint.common.server.util.ByteUtils;
import com.navercorp.pinpoint.common.server.util.NumberPrecondition;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class SpanBo implements Event, BasicSpan {

    private static final int UNDEFINED = ServiceType.UNDEFINED.getCode();

    // version 0 means that the type of prefix's size is int
    private byte version = 0;

    //  private AgentKeyBo agentKeyBo;
    @NonNull
    private String agentId;
    private String agentName;

    @NonNull
    private String applicationName;

    private long agentStartTime;

    private ServerTraceId transactionId;

    private long spanId;
    private long parentSpanId;

    private String parentApplicationName;
    private short parentApplicationServiceType;

    private long startTime;
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

    private String parentServiceName;

    public SpanBo() {
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

    @Override
    public ServerTraceId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(ServerTraceId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(String agentId) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Deprecated
    @Override
    public String getApplicationId() {
        return getApplicationName();
    }

    @Deprecated
    @Override
    public void setApplicationId(String applicationName) {
        setApplicationName(applicationName);
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = NumberPrecondition.requirePositiveOrZero(agentStartTime, "agentStartTime");
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


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

    public String getParentApplicationName() {
        return parentApplicationName;
    }

    public void setParentApplicationName(String parentApplicationName) {
        this.parentApplicationName = parentApplicationName;
    }

    public short getParentApplicationServiceType() {
        return parentApplicationServiceType;
    }

    public void setParentApplicationServiceType(short parentApplicationServiceType) {
        this.parentApplicationServiceType = parentApplicationServiceType;
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

    public String getParentServiceName() {
        return parentServiceName;
    }

    public void setParentServiceName(String parentServiceName) {
        this.parentServiceName = parentServiceName;
    }

    @Override
    public String toString() {
        return "SpanBo{" +
                "version=" + version +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", parentApplicationName='" + parentApplicationName + '\'' +
                ", parentApplicationServiceType=" + parentApplicationServiceType +
                ", startTime=" + startTime +
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
                ", parentServiceName='" + parentServiceName + '\'' +
                '}';
    }

    public static Builder newBuilder(long spanId) {
        return new Builder(spanId);
    }

    public static class Builder {

        private int version = 0;

        private String agentId;
        private String agentName;
        private String applicationName;
        private long agentStartTime;

        private ServerTraceId transactionId;

        private final long spanId;

        private long parentSpanId;

        private String parentApplicationName;
        private short parentApplicationServiceType;

        private long startTime;
        private int elapsed;

        private String rpc;
        private int serviceType;
        private String endPoint;
        private int apiId;

        private final List<AnnotationBo> annotationBoList = new ArrayList<>();
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

        private String parentServiceName;

        Builder(long spanId) {
            this.spanId = spanId;
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder setAgentId(String agentId) {
            this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
            return this;
        }

        public Builder setAgentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        /**
         * @deprecated 3.1.0 Use {@link #setApplicationName(String)} instead.
         */
        @Deprecated
        public Builder setApplicationId(String applicationName) {
            return setApplicationName(applicationName);
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
            return this;
        }

        public Builder setAgentStartTime(long agentStartTime) {
            this.agentStartTime = agentStartTime;
            return this;
        }

        public Builder setTransactionId(ServerTraceId transactionId) {
            this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
            return this;
        }

        public Builder setParentSpanId(long parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder setParentApplicationId(String parentApplicationName) {
            this.parentApplicationName = parentApplicationName;
            return this;
        }

        public Builder setParentApplicationServiceType(short parentApplicationServiceType) {
            this.parentApplicationServiceType = parentApplicationServiceType;
            return this;
        }

        public Builder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setElapsed(int elapsed) {
            this.elapsed = elapsed;
            return this;
        }

        public Builder setRpc(String rpc) {
            this.rpc = rpc;
            return this;
        }

        public Builder setServiceType(int serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder setEndPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder setApiId(int apiId) {
            this.apiId = apiId;
            return this;
        }

        public Builder setFlag(short flag) {
            this.flag = flag;
            return this;
        }

        public Builder setErrCode(int errCode) {
            this.errCode = errCode;
            return this;
        }

        public Builder setCollectorAcceptTime(long collectorAcceptTime) {
            this.collectorAcceptTime = collectorAcceptTime;
            return this;
        }

        public Builder setExceptionInfo(ExceptionInfo exceptionInfo) {
            this.exceptionInfo = exceptionInfo;
            return this;
        }

        public Builder setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
            return this;
        }

        public Builder setApplicationServiceType(int applicationServiceType) {
            this.applicationServiceType = applicationServiceType;
            return this;
        }

        public Builder setAcceptorHost(String acceptorHost) {
            this.acceptorHost = acceptorHost;
            return this;
        }

        public Builder setRemoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder setLoggingTransactionInfo(byte loggingTransactionInfo) {
            this.loggingTransactionInfo = loggingTransactionInfo;
            return this;
        }

        public Builder addAnnotationBo(AnnotationBo e) {
            this.annotationBoList.add(e);
            return this;
        }

        public Builder addSpanEventBo(SpanEventBo e) {
            this.spanEventBoList.add(e);
            return this;
        }

        public Builder setParentServiceName(String parentServiceName) {
            this.parentServiceName = parentServiceName;
            return this;
        }

        public SpanBo build() {
            SpanBo result = new SpanBo();
            result.setVersion(this.version);
            result.setAgentId(StringPrecondition.requireHasLength(this.agentId, "agentId"));
            result.setAgentName(this.agentName);
            result.setApplicationName(StringPrecondition.requireHasLength(this.applicationName, "applicationName"));
            result.setAgentStartTime(this.agentStartTime);
            result.setTransactionId(this.transactionId);
            result.setSpanId(this.spanId);
            result.setParentSpanId(this.parentSpanId);
            result.setParentApplicationName(this.parentApplicationName);
            result.setParentApplicationServiceType(this.parentApplicationServiceType);
            result.setParentServiceName(this.parentServiceName);
            result.setStartTime(this.startTime);
            result.setElapsed(this.elapsed);
            result.setRpc(this.rpc);
            result.setServiceType(this.serviceType);
            result.setEndPoint(this.endPoint);
            result.setApiId(this.apiId);
            result.setFlag(this.flag);
            result.setErrCode(this.errCode);
            result.setCollectorAcceptTime(this.collectorAcceptTime);
            result.setExceptionClass(this.exceptionClass);
            if (this.exceptionInfo != null) {
                result.setExceptionInfo(exceptionInfo);
            }
            result.setApplicationServiceType(this.applicationServiceType);
            result.setAcceptorHost(this.acceptorHost);
            result.setRemoteAddr(this.remoteAddr);
            result.setLoggingTransactionInfo(this.loggingTransactionInfo);
            result.setAnnotationBoList(this.annotationBoList);
            result.addSpanEventBoList(this.spanEventBoList);
            return result;
        }
    }
}
