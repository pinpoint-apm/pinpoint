package com.profiler.context;

import org.junit.Test;

public class SpanTest {

	@Test
	public void span() {
		Span rootSpan = RequestContext.getSpan(TraceID.EMPTY, SpanID.ROOT_SPAN_ID, "UnitTest", true);

		Annotation a1 = new Annotation(System.nanoTime(), "step1", EndPoint.NONE);
		Annotation a2 = new Annotation(System.nanoTime(), "step2", EndPoint.NONE);
		Annotation a3 = new Annotation(System.nanoTime(), "step2", new EndPoint("HTTP", "127.0.0.1", 1111, "localserver"));

		a1.processStart();
		a1.processEnd();

		a2.processStart();
		a2.processEnd();

		a3.processStart();
		a3.processEnd();

		rootSpan.addAnnotation(a1);
		rootSpan.addAnnotation(a2);
		rootSpan.addAnnotation(a3);

		System.out.println(rootSpan);
	}
}
