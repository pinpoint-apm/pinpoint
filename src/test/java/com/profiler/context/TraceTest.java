package com.profiler.context;

import org.junit.Test;

import com.profiler.context.tracer.DefaultTracer;

public class TraceTest {

	@Test
	public void trace() {
		Trace.addTracer(new DefaultTracer());
		TraceID nextId = Trace.getNextId();
		nextId.setSampled(Trace.getTraceId().isSampled());

		Trace.setTraceId(nextId);
		
		// http server receive
		Trace.recordRpcName("service_name", "http://");
		Trace.recordServerAddr("localhost", 8080);
		Trace.record(new Annotation.ServerRecv());

		// get data form db
		getDataFromDB();

		// response to client
		Trace.record(new Annotation.ServerSend());
	}

	private void getDataFromDB() {
		// db server request
		Trace.recordRpcName("mysql", "mysql");
		Trace.record("query");
		Trace.record(new Annotation.ClientSend());

		// get a db response
		Trace.record(new Annotation.ClientRecv());
	}
}
