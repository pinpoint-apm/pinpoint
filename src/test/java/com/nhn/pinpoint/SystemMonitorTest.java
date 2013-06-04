package com.nhn.pinpoint;

import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.context.DefaultTraceContext;
import com.nhn.pinpoint.sender.DataSender;
import com.nhn.pinpoint.sender.LoggingDataSender;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: emeroad
 * Date: 12. 9. 24
 * Time: 오후 3:54
 * To change this template use File | Settings | File Templates.
 */
public class SystemMonitorTest {
    @Test
    public void testStart() throws Exception {
        DataSender loggingDataSender = new LoggingDataSender();
        TraceContext traceContext = new DefaultTraceContext();
        ProfilerConfig profilerConfig = new ProfilerConfig();
        
        SystemMonitor systemMonitor = new SystemMonitor(traceContext, profilerConfig);
        systemMonitor.setDataSender(loggingDataSender);
        systemMonitor.start();

        Thread.sleep(10000);

        systemMonitor.stop();
    }

    @Test
    public void testStop() throws Exception {

    }
}
