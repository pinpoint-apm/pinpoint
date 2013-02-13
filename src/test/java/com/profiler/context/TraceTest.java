package com.profiler.context;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.sender.DataSender;

import com.profiler.sender.LoggingDataSender;
import org.apache.thrift.TBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void trace() {
        TraceID traceID = TraceID.newTraceId();
        Trace trace = new Trace(traceID);
        trace.traceBlockBegin();

        // http server receive
        trace.recordRpcName(ServiceType.UNKNOWN, "service_name", "http://");
        trace.recordEndPoint("http:localhost:8080");
        trace.recordAttribute(AnnotationKey.API, "VALUE");

        // get data form db
        getDataFromDB(trace);

        // response to client

        trace.traceBlockEnd();
    }


    @Test
    public void popEventTest() {
        TraceID traceID = TraceID.newTraceId();
        Trace trace = new Trace(traceID);
        TestDataSender dataSender = new TestDataSender();
        BypassStorage bypassStorage = new BypassStorage();
        bypassStorage.setDataSender(new LoggingDataSender());
        trace.setStorage(bypassStorage);
//        trace.traceBlockBegin();

        // response to client

        trace.traceBlockEnd(0);

        logger.info(String.valueOf(dataSender.event));
    }

    public class TestDataSender implements DataSender {
        public boolean event;

        @Override
        public boolean send(TBase<?, ?> data) {
            event = true;
            return false;
        }

        @Override
        public boolean send(Thriftable thriftable) {
            this.event = true;
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void stop() {
        }
    }


    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();

        // db server request
        trace.recordRpcName(ServiceType.MYSQL, "mysql", "rpc");
        trace.recordAttribute(AnnotationKey.SQL, "SELECT * FROM TABLE");

        // get a db response

        trace.traceBlockEnd();


    }
}
