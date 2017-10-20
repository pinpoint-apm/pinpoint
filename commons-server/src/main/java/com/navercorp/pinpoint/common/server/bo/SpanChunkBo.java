package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.util.TransactionId;

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



    public SpanChunkBo() {
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
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

    @Override
    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
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
                '}';
    }
}
