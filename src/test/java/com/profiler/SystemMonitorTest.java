package com.profiler;

import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;
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

        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.setDataSender(loggingDataSender);
        systemMonitor.start();

        Thread.sleep(10000);

        systemMonitor.stop();
    }

    @Test
    public void testStop() throws Exception {

    }
}
