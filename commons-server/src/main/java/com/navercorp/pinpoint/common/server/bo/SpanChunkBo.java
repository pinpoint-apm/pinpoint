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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkBo implements BasicSpan {

    private byte version = 0;

    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private TransactionId transactionId;

    private long spanId;
    private String endPoint;

    @Deprecated
    private short serviceType;
    private Short applicationServiceType;

    private List<SpanEventBo> spanEventBoList = new ArrayList<SpanEventBo>();

    private long collectorAcceptTime;

    private LocalAsyncIdBo localAsyncId;
    private long keyTime;


    public SpanChunkBo() {
    }

    @Override
    public int getVersion() {
        return version & 0xFF;
    }

    public void setVersion(int version) {
        SpanBo.checkVersion(version);
        // check range
        this.version = (byte) (version & 0xFF);
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    @Override
    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
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

    public long getKeyTime() {
        return this.keyTime;
    }

    public void setKeyTime(long keyTime) {
        this.keyTime = keyTime;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public void setApplicationServiceType(Short applicationServiceType) {
        this.applicationServiceType  = applicationServiceType;
    }

    @Deprecated
    public short getServiceType() {
        return serviceType;
    }

    @Deprecated
    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
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

    public List<SpanEventBo> getSpanEventBoList() {
        return spanEventBoList;
    }

    public void addSpanEventBoList(List<SpanEventBo> spanEventBoList) {
        if (spanEventBoList == null) {
            return;
        }
        this.spanEventBoList.addAll(spanEventBoList);
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
                ", agentId='" + agentId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", endPoint='" + endPoint + '\'' +
                ", serviceType=" + serviceType +
                ", applicationServiceType=" + applicationServiceType +
                ", spanEventBoList=" + spanEventBoList +
                ", collectorAcceptTime=" + collectorAcceptTime +
                ", localAsyncId=" + localAsyncId +
                ", keyTIme=" + keyTime +
                '}';
    }
}
