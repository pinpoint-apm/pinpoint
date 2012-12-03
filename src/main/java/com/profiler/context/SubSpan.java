package com.profiler.context;

import com.profiler.Agent;
import com.profiler.common.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class SubSpan implements Thriftable {

    private final Span parentSpan;

    private short sequence;

    private long startTime;
    private long endTime;
    private String serviceName;
    private String rpc;
    private ServiceType serviceType;
    private String endPoint;

    private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);

    public SubSpan(Span parentSpan) {
        this.parentSpan = parentSpan;
    }

    public Span getParentSpan() {
        return parentSpan;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    public boolean addAnnotation(HippoAnnotation annotation) {
        return annotations.add(annotation);
    }

    public int getAnnotationSize() {
        return annotations.size();
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\n\t ParentTraceID=").append(parentSpan.getTraceID());
        sb.append("\n\t sequence=").append(sequence);
        sb.append(",\n\t StartTime=").append(startTime);
        sb.append(", EndTime=").append(endTime);
        sb.append(",\n\t Name=").append(rpc);
        sb.append(", ServiceName=").append(serviceName);
        sb.append(", ServiceType=").append(serviceType);
        sb.append(", EndPoint=").append(endPoint);
        sb.append(", Seq=").append(sequence);
        sb.append(",\n\t Annotations = {");
        for (HippoAnnotation a : annotations) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.profiler.common.dto.thrift.SubSpan toThrift() {
        com.profiler.common.dto.thrift.SubSpan span = new com.profiler.common.dto.thrift.SubSpan();

        span.setAgentId(Agent.getInstance().getAgentId());
        long parentSpanStartTime = parentSpan.getStartTime();
        span.setStartElapsed((int) (startTime - parentSpanStartTime));
        span.setEndElapsed((int) (endTime - startTime));
        TraceID parentSpanTraceID = parentSpan.getTraceID();
        span.setMostTraceId(parentSpanTraceID.getId().getMostSignificantBits());
        span.setLeastTraceId(parentSpanTraceID.getId().getLeastSignificantBits());
        span.setRpc(rpc);
        span.setServiceName(serviceName);
        span.setServiceType(serviceType.getCode());

        span.setSpanId(parentSpanTraceID.getSpanId());
        span.setSequence(sequence);

        span.setEndPoint(endPoint);

        // 여기서 데이터 인코딩을 하자.
        List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(annotations.size());
        for (HippoAnnotation a : annotations) {
            annotationList.add(a.toThrift());
        }
        span.setAnnotations(annotationList);

        return span;
    }
}
