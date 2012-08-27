package com.profiler.context;

public class Record {

	private final TraceID traceId;
	private final long timestamp;
	private final Annotation annotation;
	private final Long duration;

	public Record(TraceID traceId, long timestamp, Annotation annotation, Long duration) {
		this.traceId = traceId;
		this.timestamp = timestamp;
		this.annotation = annotation;
		this.duration = duration;
	}

	public TraceID getTraceId() {
		return this.traceId;
	}

	public Annotation getAnnotation() {
		return this.annotation;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public Long getDuration() {
		return this.duration;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Record={");
		sb.append("traceId=").append(traceId);
		sb.append(", timestamp=").append(timestamp);
		sb.append(", annotation=").append(annotation);
		sb.append(", duration=").append(duration);
		sb.append("}");

		return sb.toString();
	}

}
