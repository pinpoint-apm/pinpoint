package com.profiler.context.tracer;

import com.profiler.context.Annotation;
import com.profiler.context.Annotation.ClientAddr;
import com.profiler.context.Annotation.RpcName;
import com.profiler.context.Annotation.ServerAddr;
import com.profiler.context.DeadlineSpanMap;
import com.profiler.context.EndPoint;
import com.profiler.context.Record;
import com.profiler.context.Span;
import com.profiler.context.SpanUpdater;
import com.profiler.context.TraceID;

public class DefaultTracer implements Tracer {

	private final DeadlineSpanMap spanMap = new DeadlineSpanMap(this);

	private void mutate(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = spanMap.update(traceId, spanUpdater);

		if (span.isExistsAnnotationType("CR") || span.isExistsAnnotationType("SS")) {
			spanMap.remove(traceId);
			logSpan(span);
		}
	}

	private void annotate(final Record record, final String value) {
		mutate(record.getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				span.addAnnotation(new HippoAnnotation(record.getTimestamp(), value, span.getEndPoint(), record.getDuration()));
				return span;
			}
		});
	}

	private void setEndPoint(Record record, final EndPoint endPoint) {
		mutate(record.getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				// set endpoint to both span and annotations
				span.setEndPoint(endPoint);
				return span;
			}
		});
	}

	private void logSpan(Span span) {
		// TODO: send span to server
		System.out.println("Write span=" + span);
	}

	@Override
	public void record(final Record record) {
		final Annotation ann = record.getAnnotation();

		if (ann instanceof Annotation.ClientSend) {
			annotate(record, "CS");
		} else if (ann instanceof Annotation.ClientRecv) {
			annotate(record, "CR");
		} else if (ann instanceof Annotation.ServerSend) {
			annotate(record, "SS");
		} else if (ann instanceof Annotation.ServerRecv) {
			annotate(record, "SR");
		} else if (ann instanceof Annotation.Message) {
			annotate(record, ((Annotation.Message) ann).getMessage());
		} else if (ann instanceof Annotation.RpcName) {
			mutate(record.getTraceId(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					RpcName a = (RpcName) ann;
					span.setName(a.getRpc());
					span.setServiceName(a.getService());
					return span;
				}
			});
		} else if (ann instanceof Annotation.ClientAddr) {
			ClientAddr a = (ClientAddr) ann;
			setEndPoint(record, new EndPoint(a.getIp(), a.getPort()));
		} else if (ann instanceof Annotation.ServerAddr) {
			ServerAddr a = (ServerAddr) ann;
			setEndPoint(record, new EndPoint(a.getIp(), a.getPort()));
		}
	}
}
