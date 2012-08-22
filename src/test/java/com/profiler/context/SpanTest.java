package com.profiler.context;

import org.junit.Test;

import com.profiler.context.gen.Annotation;
import com.profiler.context.gen.BinaryAnnotation;

public class SpanTest {

	@Test
	public void span() {
		Trace tracer = RequestContext.getTrace(TraceID.EMPTY, SpanID.ROOT_SPAN_ID, "UnitTest", true);

		Annotation a1 = new Annotation(System.nanoTime(), "step1");
		Annotation a2 = new Annotation(System.nanoTime(), "step2");
		BinaryAnnotation a3 = new BinaryAnnotation(System.nanoTime(), "", null, "");

		tracer.record(a1);
		tracer.record(a2);
		tracer.recordBinary(a3);

		System.out.println(tracer);
	}
}
