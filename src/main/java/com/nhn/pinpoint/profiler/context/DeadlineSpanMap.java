package com.nhn.pinpoint.profiler.context;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Deprecated
public class DeadlineSpanMap {

	private static final long FLUSH_TIMEOUT = 120000L; // 2 minutes

	private final ConcurrentMap<DefaultTraceId.TraceKey, Span> map = new ConcurrentHashMap<DefaultTraceId.TraceKey, Span>(256);

	private final Timer timer = new Timer(true);

	public Span update(DefaultTraceId traceId) {
		DefaultTraceId.TraceKey traceIdKey = traceId.getTraceKey();
		Span span = map.get(traceIdKey);

		if (span == null) {
			span = new Span();
			map.put(traceIdKey, span);

			TimerTask task = new FlushTimedoutSpanTask(span);
			// span.setTimerTask(task);

			timer.schedule(task, FLUSH_TIMEOUT);
		}

		return span;
	}

	public Span remove(DefaultTraceId traceId) {
		return map.remove(traceId.getTraceKey());
	}

	public int size() {
		return map.size();
	}

	private static final class FlushTimedoutSpanTask extends TimerTask {
		private final Span span;

		public FlushTimedoutSpanTask(Span span) {
			this.span = span;
		}

		@Override
		public void run() {
			// Trace.logSpan(this.span);
		}
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
