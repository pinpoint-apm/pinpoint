package com.profiler.context.tracer;

import com.profiler.context.Annotation;
import com.profiler.context.DeadlineSpanMap;
import com.profiler.context.Record;
import com.profiler.context.Span;
import com.profiler.context.SpanUpdater;
import com.profiler.context.TraceID;

public class DefaultTracer implements Tracer {

	private final DeadlineSpanMap spanMap = new DeadlineSpanMap(this);

	private void mutate(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = spanMap.update(traceId, spanUpdater);

		if (span.isExistsAnnotation(Annotation.CLIENT_RECV) || span.isExistsAnnotation(Annotation.SERVER_SEND)) {
			spanMap.remove(traceId);
			logSpan(span);
		}
	}

	private void logSpan(Span span) {
		System.out.println("Write span=" + span);
	}

	@Override
	public void record(final Record record) {
		mutate(record.getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				span.addAnnotation(record.getAnnotation());
				return span;
			}
		});

	}
}
