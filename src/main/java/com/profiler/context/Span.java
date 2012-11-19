package com.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.profiler.Agent;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span {

    private final TraceID traceID;

    private long startTime;

    private long endTime;

    private String serviceName;
    private String name;
    private String endPoint;
    private boolean isTerminal = false;

    private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);

    public Span(TraceID traceId, String name, String endPoint) {
        this.traceID = traceId;
        this.name = name;
        this.endPoint = endPoint;
    }

    public TraceID getTraceID() {
        return traceID;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\n\t TraceID = ").append(traceID);
        sb.append(",\n\t StartTime = ").append(startTime);
        sb.append(",\n\t EndTime = ").append(endTime);
        sb.append(",\n\t Name = ").append(name);
        sb.append(",\n\t ServiceName = ").append(serviceName);
        sb.append(",\n\t EndPoint = ").append(endPoint);

        sb.append(",\n\t Annotations = {");
        for (HippoAnnotation a : annotations) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.profiler.common.dto.thrift.Span toThrift() {
        com.profiler.common.dto.thrift.Span span = new com.profiler.common.dto.thrift.Span();

        span.setAgentId(Agent.getInstance().getAgentId());
        span.setTimestamp(startTime);
        // TODO api를 생성하고 여기를 고치자.
        //span.setEndTime(startTime);
        span.setMostTraceId(traceID.getId().getMostSignificantBits());
        span.setLeastTraceId(traceID.getId().getLeastSignificantBits());
        span.setName(name);
        span.setServiceName(serviceName);
        span.setSpanId(traceID.getSpanId());
        span.setParentSpanId(traceID.getParentSpanId());
        span.setEndPoint(endPoint);
        span.setTerminal(isTerminal);

        // 여기서 데이터 인코딩을 하자.
        List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(annotations.size());
        for (HippoAnnotation a : annotations) {
            annotationList.add(a.toThrift());
        }
        span.setAnnotations(annotationList);

        span.setFlag(traceID.getFlags());

        return span;
    }
}
