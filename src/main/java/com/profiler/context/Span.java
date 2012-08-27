package com.profiler.context;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A span represents one RPC request. A trace is made up of many spans.
 * 
 * @author netspider
 * 
 */
public class Span {

	private final TraceID traceID;
	private final String name;
	private final EndPoint endPoint;

	private final long createTime;

	private final SortedSet<Annotation> annotations = new TreeSet<Annotation>(new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
			throw new RuntimeException("Comparator not implemented");
			// return (int) (a1.getTimestamp() - a2.getTimestamp());
		}
	});

	public Span(TraceID traceId, String name, EndPoint endPoint) {
		this.traceID = traceId;
		this.name = name;
		this.endPoint = endPoint;

		this.createTime = System.nanoTime();
	}

	public boolean addAnnotation(Annotation annotation) {
		return annotations.add(annotation);
	}

	public int getAnnotationSize() {
		return annotations.size();
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
