package com.nhn.pinpoint.web.path;

import java.text.SimpleDateFormat;

import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpan;

/**
 *
 */
public class PathTest {
	int index;
	SimpleDateFormat format = new SimpleDateFormat("ss.SSS");

	@Test
	public void testPath() {
		// root -> next1 -> next2
		// > - -> next3

		// Span root = root();
		// printSpan("root", root);
		//
		// Span next1 = createNextSpan(root);
		// printSpan("next1", next1);
		//
		// Span next2 = createNextSpan(next1);
		// printSpan("next2", next2);
		//
		// Span next3 = createNextSpan(next1);
		// printSpan("next3", next3);

	}

	public void printSpan(String msg, TSpan span) {
		// System.out.println(msg + " id:" + span.getSpanID() + " pid:" +
		// span.getParentSpanId() + " time:" + format.format(new
		// Date(span.getTimestamp())));
		System.out.println(msg + " id:" + span.getSpanId() + " pid:" + span.getParentSpanId() + " time:" + span.getStartTime());
	}
	// private Span root() {
	// TraceID traceID = TraceID.newTraceId();
	// UUID uuid = traceID.getTransactionSequence();
	// Span root = new Span("test", System.currentTimeMillis(),
	// uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(),
	// Integer.toString(index++), "serviceName", traceID.getSpanId(), null,
	// null, "http:ip:23");
	// root.setParentSpanId(traceID.getParentSpanId());
	// return root;
	// }

	// private Span createNextSpan(Span span) {
	// UUID uuid = new UUID(span.getMostTraceID(), span.getLeastTraceID());
	// TraceID traceID = new TraceID(uuid, span.getParentSpanId(),
	// span.getSpanID(), true, 0);
	// TraceID nextTraceId = traceID.getNextTraceId();
	// Span next = new Span("test", System.currentTimeMillis(),
	// span.getMostTraceID(), span.getLeastTraceID(), Integer.toString(index++),
	// "serviceName", nextTraceId.getSpanId(), null, null, "http:ip:23");
	// next.setParentSpanId(nextTraceId.getParentSpanId());
	// return next;
	// }
}
