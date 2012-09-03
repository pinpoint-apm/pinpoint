package com.profiler.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public Span(TraceID traceId, String name, EndPoint endPoint) {
		this.traceID = traceId;
		this.name = name;
		this.endPoint = endPoint;
		this.createTime = System.nanoTime();
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
		for (HippoAnnotation annotation : annotations) {
			annotation.setEndPoint(endPoint);
		}

		for (HippoBinaryAnnotation annotation : binaryAnnotations) {
			annotation.setEndPoint(endPoint);
		}
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
		sb.append("}");

		return sb.toString();
	}
}
