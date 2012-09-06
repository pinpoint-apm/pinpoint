package com.profiler.context;

import org.junit.Test;

public class TraceTest {

	@Test
	public void trace() {
		Trace.traceBlockBegin();
		Trace.setTraceId(TraceID.newTraceId());

		// http server receive
		Trace.recordRpcName("service_name", "http://");
		Trace.recordEndPoint("localhost", 8080);
		Trace.recordAttibute("KEY", "VALUE");
		Trace.record(Annotation.ServerRecv);

		// get data form db
		getDataFromDB();

		// response to client
		Trace.record(Annotation.ServerSend);

		Trace.traceBlockEnd();
	}

	private void getDataFromDB() {
		Trace.traceBlockBegin();

		// db server request
		Trace.recordRpcName("mysql", "rpc");
		Trace.recordAttibute("mysql.query", "SELECT * FROM TABLE");
		Trace.record(Annotation.ClientSend);

		// get a db response
		Trace.record(Annotation.ClientRecv);

		Trace.traceBlockEnd();
	}
}
