package com.profiler.context.tracer;

import com.profiler.context.Record;

public class DefaultTracer implements Tracer {
	@Override
	public void record(Record record) {
		System.out.println("record=" + record);
	}
}
