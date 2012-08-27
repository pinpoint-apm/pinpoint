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
	private final String name;
	private final EndPoint endPoint;
	private final long createTime;

	private final List<Annotation> annotations = new ArrayList<Annotation>();
	private final Set<String> annotationDesc = new HashSet<String>();

	public Span(TraceID traceId, String name, EndPoint endPoint) {
		this.traceID = traceId;
		this.name = name;
		this.endPoint = endPoint;
		this.createTime = System.nanoTime();
	}

	public boolean addAnnotation(Annotation annotation) {
		annotationDesc.add(annotation.toString());
		return annotations.add(annotation);
	}

	public int getAnnotationSize() {
		return annotations.size();
	}

	public boolean isExistsAnnotation(String annotation) {
		return annotationDesc.contains(annotation);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Span={");
		sb.append("TraceID=").append(traceID);
		sb.append(", CreateTime=").append(createTime);
		sb.append(", Name=").append(name);
		sb.append(", EndPoint=").append(endPoint);
		sb.append(", Annotations=").append(annotations);
		sb.append("}");

		return sb.toString();
	}
}
