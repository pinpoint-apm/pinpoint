package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import com.nhn.pinpoint.profiler.context.BypassStorage;
import com.nhn.pinpoint.profiler.context.DefaultTrace;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DefaultTraceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeClass
    public static void before() throws Exception {
        Slf4jLoggerBinderInitializer.beforeClass();
    }

    @AfterClass
    public static void after()  throws Exception {
        Slf4jLoggerBinderInitializer.afterClass();
    }


    @Test
    public void testPushPop() {
        DefaultTrace trace = new DefaultTrace("agent", 0, 1);
        BypassStorage bypassStorage = new BypassStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);
        trace.setStorage(bypassStorage);

        Assert.assertEquals(0, trace.getCallStackDepth());

        trace.traceBlockBegin();

        Assert.assertEquals(1, trace.getCallStackDepth());

        trace.traceBlockBegin();
        Assert.assertEquals(2, trace.getCallStackDepth());

        trace.traceBlockEnd();

        Assert.assertEquals(1, trace.getCallStackDepth());

        trace.traceBlockEnd();

        Assert.assertEquals(0, trace.getCallStackDepth());

        trace.traceRootBlockEnd();

    }
}
