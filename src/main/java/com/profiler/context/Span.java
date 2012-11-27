package com.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.profiler.Agent;
import com.profiler.common.ServiceType;

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
	private boolean isTerminal = false;

	private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);

	public Span(TraceID traceId) {
		this.traceID = traceId;
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

	public String getRpc() {
		return rpc;
	}

	public void setRpc(String rpc) {
		this.rpc = rpc;
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

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
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
