package com.profiler.context;

/**
 * 
 * @author netspider
 * 
 */
public class TraceIDStack {

	private TraceID[] traceIDs = new TraceID[1];

	private volatile int index = 0;

	public TraceID getTraceId() {
		return traceIDs[index];
	}

	public TraceID getParentTraceId() {
		if (index > 0) {
			return traceIDs[index - 1];
		}
		System.out.println("PID is NULL");
		return null;
	}

	public void setTraceId(TraceID traceId) {
		traceIDs[index] = traceId;
	}

	public void incr() {
		index++;
		if (index > traceIDs.length - 1) {
			System.out.println("INCR");
			TraceID[] old = traceIDs;
			traceIDs = new TraceID[index + 1];
			System.arraycopy(old, 0, traceIDs, 0, old.length);
		}
	}

	public void decr() {
		if (index > 0)
			index--;
	}

	public void clear() {
		traceIDs[index] = null;
	}
}
