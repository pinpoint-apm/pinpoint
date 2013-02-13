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
public class Span implements Thriftable {

    private final TraceID traceID;
    private long startTime;
    private long endTime;
    private String serviceName;
    private String rpc;
    private ServiceType serviceType;
    private String endPoint;
    private int exception;
    private String remoteAddr;

    private final List<Annotation> annotations = new ArrayList<Annotation>(5);

    private List<SubSpan> subSpanList;
    
    public Span(TraceID traceId) {
        this.traceID = traceId;
    }

    public TraceID getTraceID() {
        return traceID;
    }

    public boolean addAnnotation(Annotation annotation) {
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

    public List<SubSpan> getSubSpanList() {
        return subSpanList;
    }

    public void setSubSpanList(List<SubSpan> subSpanList) {
        this.subSpanList = subSpanList;
    }
    
    public int getException() {
		return exception;
	}

	public void setException(int exception) {
		this.exception = exception;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\n\t TraceID = ").append(traceID);
        sb.append(",\n\t StartTime = ").append(startTime);
        sb.append(", EndTime = ").append(endTime);
        sb.append(",\n\t Name = ").append(rpc);
        sb.append(", ServiceName = ").append(serviceName);
        sb.append(", ServiceType = ").append(serviceType);
        sb.append(", EndPoint = ").append(endPoint);
        sb.append(", Exception = ").append(exception);
        sb.append(", RemoteAddr = ").append(remoteAddr);
        sb.append(",\n\t Annotations = {");
        for (Annotation a : annotations) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.profiler.common.dto.thrift.Span toThrift() {
        com.profiler.common.dto.thrift.Span span = new com.profiler.common.dto.thrift.Span();

        span.setAgentId(Agent.getInstance().getAgentId());
        span.setAgentIdentifier(Agent.getInstance().getIdentifier());

        span.setStartTime(startTime);
        span.setElapsed((int) (endTime - startTime));
        span.setMostTraceId(traceID.getId().getMostSignificantBits());
        span.setLeastTraceId(traceID.getId().getLeastSignificantBits());
        span.setRpc(rpc);
        span.setServiceName(serviceName);
        span.setServiceType(serviceType.getCode());
        span.setSpanId(traceID.getSpanId());
        span.setParentSpanId(traceID.getParentSpanId());
        span.setEndPoint(endPoint);
        span.setRemoteAddr(remoteAddr);
        if (exception != 0) {
            span.setErr(exception);
        }

        // 여기서 데이터 인코딩을 하자.
        List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(annotations.size());
        for (Annotation a : annotations) {
            annotationList.add(a.toThrift());
        }
        span.setAnnotations(annotationList);

        span.setFlag(traceID.getFlags());

        List<SubSpan> subSpanList = this.getSubSpanList();
        if (subSpanList != null && subSpanList.size() != 0) {

            List<com.profiler.common.dto.thrift.SubSpan> tSubSpanList = new ArrayList<com.profiler.common.dto.thrift.SubSpan>(subSpanList.size());
            for (SubSpan subSpan : subSpanList) {
                com.profiler.common.dto.thrift.SubSpan tSubSpan = subSpan.toThrift(true);
                tSubSpanList.add(tSubSpan);
            }
            span.setSubSpanList(tSubSpanList);
        }

        return span;
    }
}
