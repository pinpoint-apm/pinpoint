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
import com.navercorp.pinpoint.common.server.util.ByteUtils;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanChunkBo implements BasicSpan {

    private static final int UNDEFINED = ServiceType.UNDEFINED.getCode();

    private byte version = 0;

    @NotBlank private String agentId;
    private String agentName;
    @NotBlank private String applicationName;
    @PositiveOrZero private long agentStartTime;

    private TransactionId transactionId;

    private long spanId;
    private String endPoint;

    private int applicationServiceType;

    private final List<SpanEventBo> spanEventBoList = new ArrayList<>();

    private long collectorAcceptTime;

    private LocalAsyncIdBo localAsyncId;
    private long keyTime;


    public SpanChunkBo() {
    }

    @Override
    public int getVersion() {
        return Byte.toUnsignedInt(version);
    }

    public void setVersion(int version) {
        this.version = ByteUtils.toUnsignedByte(version);
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Deprecated
    @Override
    public String getApplicationId() {
        return getApplicationName();
    }

    @Deprecated
    public void setApplicationId(String applicationName) {
        setApplicationName(applicationName);
    }


    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
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
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentStartTime=" + agentStartTime +
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
