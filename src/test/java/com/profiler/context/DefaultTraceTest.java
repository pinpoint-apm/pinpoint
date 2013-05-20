package com.profiler.context;

import com.profiler.logging.Slf4jLoggerBinderInitializer;
import com.profiler.sender.LoggingDataSender;
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
        DefaultTrace trace = new DefaultTrace();
        BypassStorage bypassStorage = new BypassStorage();
        bypassStorage.setDataSender(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);
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
