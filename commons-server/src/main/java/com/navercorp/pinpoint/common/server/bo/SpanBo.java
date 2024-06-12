/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanBo implements Event, BasicSpan {

    // version 0 means that the type of prefix's size is int
    private byte version = 0;

    //  private AgentKeyBo agentKeyBo;
    private AgentId agentId;
    private String agentName;
    @NotBlank private String applicationName;
    private ApplicationId applicationId;
    @PositiveOrZero private long agentStartTime;

    private TransactionId transactionId;

    private long spanId;
    private long parentSpanId;

    private String parentApplicationName;
    private short parentApplicationServiceType;

    private long startTime;
    private int elapsed;

    private String rpc;
    private short serviceType;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList = new ArrayList<>();
    private short flag; // optional
    private int errCode;

    private final List<SpanEventBo> spanEventBoList = new ArrayList<>();
    private List<SpanChunkBo> spanChunkBoList;

    private long collectorAcceptTime;

    private boolean hasException = false;
    private int exceptionId;
    private String exceptionMessage;
    private String exceptionClass;

    private Short applicationServiceType;

    private String acceptorHost;
    private String remoteAddr; // optional

    private byte loggingTransactionInfo; //optional


    public SpanBo() {
    }

    @Override
    public int getVersion() {
        return version & 0xFF;
    }

    public byte getRawVersion() {
        return version;
    }

    public void setVersion(int version) {
        checkVersion(version);
        // check range
        this.version = (byte) (version & 0xFF);
    }

    static void checkVersion(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("out of range (0~255)");
        }
    }

    @Override
    public TransactionId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public AgentId getAgentId() {
        return agentId;
    }

    @Override
    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Override
    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
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

    public short getServiceType() {
        return serviceType;
    }

    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
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

    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public boolean isRoot() {
        return -1L == parentSpanId;
    }

    public boolean hasException() {
        return hasException;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionInfo(int exceptionId, String exceptionMessage) {
        this.hasException = true;
        this.exceptionId = exceptionId;
        this.exceptionMessage = exceptionMessage;
    }


    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public void setApplicationServiceType(Short applicationServiceType) {
        this.applicationServiceType = applicationServiceType;
    }

    public boolean hasApplicationServiceType() {
        return applicationServiceType != null;
    }

    public short getApplicationServiceType() {
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

    @Override
    public String toString() {
        return "SpanBo{" +
                "version=" + version +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", parentApplicationId='" + parentApplicationName + '\'' +
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
                ", hasException=" + hasException +
                ", exceptionId=" + exceptionId +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", exceptionClass='" + exceptionClass + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", acceptorHost='" + acceptorHost + '\'' +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", loggingTransactionInfo=" + loggingTransactionInfo +
                '}';
    }

    public static class Builder {

        private int version = 0;

        private AgentId agentId;
        private String agentName;
        private String applicationName;
        private ApplicationId applicationId;
        private long agentStartTime;

        private TransactionId transactionId;

        private final long spanId;

        private long parentSpanId;

        private String parentApplicationId;
        private short parentApplicationServiceType;

        private long startTime;
        private int elapsed;

        private String rpc;
        private short serviceType;
        private String endPoint;
        private int apiId;

        private final List<AnnotationBo> annotationBoList = new ArrayList<>();
        private short flag; // optional
        private int errCode;

        private final List<SpanEventBo> spanEventBoList = new ArrayList<>();
        private List<SpanChunkBo> spanChunkBoList;

        private long collectorAcceptTime;

        private int exceptionId;
        private String exceptionMessage;
        private String exceptionClass;

        private Short applicationServiceType;

        private String acceptorHost;
        private String remoteAddr; // optional

        private byte loggingTransactionInfo; //optional

        public Builder(long spanId) {
            this.spanId = spanId;
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder setAgentId(AgentId agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder setAgentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder setApplicationId(ApplicationId applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder setAgentStartTime(long agentStartTime) {
            this.agentStartTime = agentStartTime;
            return this;
        }

        public Builder setTransactionId(TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setParentSpanId(long parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder setParentApplicationId(String parentApplicationId) {
            this.parentApplicationId = parentApplicationId;
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

        public Builder setServiceType(short serviceType) {
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

        public Builder setExceptionId(int exceptionId) {
            this.exceptionId = exceptionId;
            return this;
        }

        public Builder setExceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
            return this;
        }

        public Builder setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
            return this;
        }

        public Builder setApplicationServiceType(Short applicationServiceType) {
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

        public SpanBo build() {
            SpanBo result = new SpanBo();
            result.setVersion(this.version);
            result.setAgentId(this.agentId);
            result.setAgentName(this.agentName);
            result.setApplicationName(this.applicationName);
            result.setApplicationId(this.applicationId);
            result.setAgentStartTime(this.agentStartTime);
            result.setTransactionId(this.transactionId);
            result.setSpanId(this.spanId);
            result.setParentSpanId(this.parentSpanId);
            result.setParentApplicationName(this.parentApplicationId);
            result.setParentApplicationServiceType(this.parentApplicationServiceType);
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
            if (this.exceptionMessage != null) {
                result.setExceptionInfo(this.exceptionId, this.exceptionMessage);
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
