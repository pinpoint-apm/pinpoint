package com.profiler.context;

import org.junit.Test;

public class TraceTest {

	@Test
	public void trace() {
		TraceID nextId = Trace.getNextId();
		nextId.setSampled(Trace.getTraceId().isSampled());

		Trace.setTraceId(nextId);

		// http server receive
		Trace.recordRpcName("service_name", "http://");
		Trace.recordEndPoint("localhost", 8080);
		Trace.record(Annotation.ServerRecv);

		// get data form db
		getDataFromDB();

		// response to client
		Trace.record(Annotation.ServerSend);
	}

	private void getDataFromDB() {
		// db server request
		Trace.recordRpcName("mysql", "mysql");
		Trace.recordMessage("query");
		Trace.record(Annotation.ClientSend);

		// get a db response
		Trace.record(Annotation.ClientRecv);
	}
}
