package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.profiler.sender.DataSender;

import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import org.apache.thrift.TBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void trace() {
        DefaultTraceID traceID = new DefaultTraceID("agent", 0, 1);
        DefaultTrace trace = new DefaultTrace(traceID);
        trace.setStorage(new BypassStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
        trace.traceBlockBegin();

        // http server receive
        trace.recordServiceType(ServiceType.UNKNOWN);
        trace.recordRpcName("http://");

        trace.recordEndPoint("http:localhost:8080");
        trace.recordAttribute(AnnotationKey.API, "VALUE");

        // get data form db
        getDataFromDB(trace);

        // response to client

        trace.traceBlockEnd();
    }


    @Test
    public void popEventTest() {
        DefaultTraceID traceID = new DefaultTraceID("agent", 0, 1);
        DefaultTrace trace = new DefaultTrace(traceID);
        TestDataSender dataSender = new TestDataSender();
        BypassStorage bypassStorage = new BypassStorage();
        bypassStorage.setDataSender(new LoggingDataSender());
        trace.setStorage(bypassStorage);
//        trace.traceBlockBegin();

        // response to client

        trace.traceRootBlockEnd();

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
            return false;
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean request(TBase<?, ?> data) {
            return send(data);
        }
    }


    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();

        // db server request
        trace.recordServiceType(ServiceType.MYSQL);
        trace.recordRpcName("rpc");

        trace.recordAttribute(AnnotationKey.SQL, "SELECT * FROM TABLE");

        // get a db response

        trace.traceBlockEnd();


    }
}
