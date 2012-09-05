package com.profiler.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

/**
 * 
 * @author netspider
 * 
 */
public class Span {

	private final TraceID traceID;
	private final long createTime;

	private String serviceName;
	private String name;
	private EndPoint endPoint;

	private final List<HippoBinaryAnnotation> binaryAnnotations = new ArrayList<HippoBinaryAnnotation>(5);
	private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);
	private final Set<String> annotationValues = new HashSet<String>(5);

	/**
	 * Cancel timer logic.
	 * TODO: refactor this.
	 */
	private TimerTask timerTask;

	public void setTimerTask(TimerTask task) {
		this.timerTask = task;
	}

	public boolean cancelTimer() {
		return timerTask.cancel();
	}

	public Span(TraceID traceId, String name, EndPoint endPoint) {
		this.traceID = traceId;
		this.name = name;
		this.endPoint = endPoint;
		this.createTime = System.currentTimeMillis();
	}

	public boolean addAnnotation(HippoAnnotation annotation) {
		annotationValues.add(annotation.getValue());
		return annotations.add(annotation);
	}

	public boolean addAnnotation(HippoBinaryAnnotation annotation) {
		return binaryAnnotations.add(annotation);
	}

	public int getAnnotationSize() {
		return annotations.size();
	}

	/**
	 * this method only works for Trace.mutate()
	 * 
	 * @param value
	 * @return
	 */
	public boolean isExistsAnnotationType(String value) {
		return annotationValues.contains(value);
	}

	public EndPoint getEndPoint() {
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

	public void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Span={");
		sb.append("TraceID=").append(traceID);
		sb.append(", CreateTime=").append(createTime);
		sb.append(", Name=").append(name);
		sb.append(", ServiceName=").append(serviceName);
		sb.append(", EndPoint=").append(endPoint);
		sb.append(", Annotations=").append(annotations);
		sb.append(", BinaryAnnotations=").append(binaryAnnotations);
		sb.append("}");

		return sb.toString();
	}

	public com.profiler.context.gen.Span toThrift() {
		com.profiler.context.gen.Span span = new com.profiler.context.gen.Span();

		span.setTimestamp(createTime);
		span.setMostTraceID(traceID.getId().getMostSignificantBits());
		span.setLeastTraceID(traceID.getId().getLeastSignificantBits());
		span.setName(name);
		span.setSpanID(traceID.getSpanId());
		span.setParentSpanId(traceID.getParentSpanId());

		List<com.profiler.context.gen.Annotation> annotationList = new ArrayList<com.profiler.context.gen.Annotation>(annotations.size());
		for (HippoAnnotation a : annotations) {
			annotationList.add(a.toThrift());
		}
		span.setAnnotations(annotationList);

		List<com.profiler.context.gen.BinaryAnnotation> binaryAnnotationList = new ArrayList<com.profiler.context.gen.BinaryAnnotation>(binaryAnnotations.size());
		for (HippoBinaryAnnotation a : binaryAnnotations) {
			binaryAnnotationList.add(a.toThrift());
		}
		span.setBinaryAnnotations(binaryAnnotationList);

		span.setFlag(traceID.getFlags());

		return span;
	}
}
