package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.AgentInformation;

import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import org.apache.thrift.TBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void trace() {
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceConetxt = getDefaultTraceConetxt();
        DefaultTrace trace = new DefaultTrace(defaultTraceConetxt , traceID);
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
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
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceConetxt = getDefaultTraceConetxt();
        DefaultTrace trace = new DefaultTrace(defaultTraceConetxt, traceID);
        TestDataSender dataSender = new TestDataSender();
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
//        trace.traceBlockBegin();

        // response to client

        trace.traceRootBlockEnd();

        logger.info(String.valueOf(dataSender.event));
    }

    private DefaultTraceContext getDefaultTraceConetxt() {
        DefaultTraceContext defaultTraceContext = new DefaultTraceContext();
        defaultTraceContext.setAgentInformation(new AgentInformation("agentId", "applicationName", System.currentTimeMillis(), 10, "test", ServiceType.TOMCAT.getCode(), Version.VERSION));
        return defaultTraceContext;
    }

    public class TestDataSender implements EnhancedDataSender {
        public boolean event;

        @Override
        public boolean send(TBase<?, ?> data) {
            event = true;
            return false;
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean request(TBase<?, ?> data) {
            return send(data);
        }

        @Override
        public boolean request(TBase<?, ?> data, int retry) {
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
