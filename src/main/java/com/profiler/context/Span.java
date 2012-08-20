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

	private final String traceID;
	private final int spanID;
	private final int parentSpanID;
	private final String name;
	private final long createTime;
	private final boolean debug;

	private final SortedSet<Annotation> annotations = new TreeSet<Annotation>(new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
			return (int) (a1.getTimestamp() - a2.getTimestamp());
		}
	});

	public Span(String traceID, int parentSpanID, String name, boolean debug) {
		if (traceID == null) {
			this.traceID = traceID;
		} else {
			this.traceID = IDFactory.newTraceID();
		}

		if (parentSpanID < 0) {
			this.spanID = 1;
		} else {
			this.spanID = ++parentSpanID;
		}

		this.parentSpanID = parentSpanID;
		this.name = name;
		this.createTime = System.nanoTime();
		this.debug = debug;
	}

	public boolean addAnnotation(Annotation annotation) {
		return annotations.add(annotation);
	}

	private void flush() {
		System.out.println("TODO: flush collected data.");
		RequestContext.removeCurrentContext();
	}

	public String getTraceID() {
		return traceID;
	}

	public int getNextSpanID() {
		return spanID + 1;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Span[");
		sb.append("TraceID=").append(traceID);
		sb.append(", SpanID=").append(spanID);
		sb.append(", ParentSpanID=").append(parentSpanID);
		sb.append(", CreateTime=").append(createTime);
		sb.append(", Name=").append(name);
		sb.append(", Annotations=").append(annotations);
		sb.append("]");

		return sb.toString();
	}
}
