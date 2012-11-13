package com.profiler.context;

import org.junit.Test;

public class TraceTest {

    @Test
    public void trace() {
        TraceID traceID = TraceID.newTraceId();
        Trace trace = new Trace(traceID);
        trace.traceBlockBegin();

        // http server receive
        trace.recordRpcName("service_name", "http://");
        trace.recordEndPoint("http:localhost:8080");
        trace.recordAttribute("KEY", "VALUE");
        trace.record(Annotation.ServerRecv);

        // get data form db
        getDataFromDB(trace);

        // response to client
        trace.record(Annotation.ServerSend);

        trace.traceBlockEnd();
    }

    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();
        trace.record(Annotation.ClientSend);

        // db server request
        trace.recordRpcName("mysql", "rpc");
        trace.recordAttribute("mysql.query", "SELECT * FROM TABLE");

        // get a db response

        trace.record(Annotation.ClientRecv);
        trace.traceBlockEnd();


    }
}
