package com.profiler.context;

import org.junit.Test;

public class SpanTest {

	@Test
	public void span() {
		Span span = RequestContext.getSpan(TraceID.EMPTY, -1, "UnitTest", true);

		
		System.out.println(span);
	}

}
