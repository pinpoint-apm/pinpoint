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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.util.TransactionId;

/**
 * @author emeroad
 */
public class SpanBo implements Event, BasicSpan {

    // version 0 means that the type of prefix's size is int
    private byte version = 0;

//  private AgentKeyBo agentKeyBo;
    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private TransactionId transactionId;

    private long spanId;
    private long parentSpanId;

    private String parentApplicationId;
    private short parentApplicationServiceType;

    private long startTime;
    private int elapsed;

    private String rpc;
    private short serviceType;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList = new ArrayList<AnnotationBo>();
    private short flag; // optional
    private int errCode;

    private List<SpanEventBo> spanEventBoList = new ArrayList<SpanEventBo>();

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

    public int getVersion() {
        return version & 0xFF;
    }


    public byte getRawVersion() {
        return version;
    }

    public void setVersion(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("out of range (0~255)");
        }
        // check range
        this.version = (byte) (version & 0xFF);
    }

    public TransactionId getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

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


    public long getSpanId() {
        return spanId;
    }

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
        this.applicationServiceType  = applicationServiceType;
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

    public String getParentApplicationId() {
        return parentApplicationId;
    }

    public void setParentApplicationId(String parentApplicationId) {
        this.parentApplicationId = parentApplicationId;
    }

    public short getParentApplicationServiceType() {
        return parentApplicationServiceType;
    }

    public void setParentApplicationServiceType(short parentApplicationServiceType) {
        this.parentApplicationServiceType = parentApplicationServiceType;
    }

    /**
     * @see com.navercorp.pinpoint.common.trace.LoggingInfo
     * @return loggingInfo key
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
                ", applicationId='" + applicationId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", parentApplicationId='" + parentApplicationId + '\'' +
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
}
