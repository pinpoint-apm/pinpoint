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

	private static final int NO_PARENT_SPAN_ID = -1;

	private final String traceID;
	private final String spanID;
	private final String parentSpanID;
	private final String name;
	private final long createTime;
	private final boolean debug;

	private final SortedSet<Annotation> annotations = new TreeSet<Annotation>(new Comparator<Annotation>() {
		@Override
		public int compare(Annotation a1, Annotation a2) {
			return (int) (a1.getTimestamp() - a2.getTimestamp());
		}
	});

	/**
	 * 
	 * @param traceID
	 * @param parentSpanID
	 * @param name
	 * @param debug
	 *            if this is set we will make sure this span is stored, no
	 *            matter what the samplers want
	 */
	public Span(String traceID, String parentSpanID, String name, boolean debug) {
		this.traceID = (traceID == null) ? TraceID.newTraceID() : traceID;
		this.spanID = SpanID.newSpanID();
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

	public boolean isDebug() {
		return debug;
	}

	public int getAnnotationSize() {
		return annotations.size();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Span={");
		sb.append("TraceID=").append(traceID);
		sb.append(", SpanID=").append(spanID);
		sb.append(", ParentSpanID=").append(parentSpanID);
		sb.append(", CreateTime=").append(createTime);
		sb.append(", Name=").append(name);
		sb.append(", Annotations=").append(annotations);
		sb.append("}");

		return sb.toString();
	}
}
