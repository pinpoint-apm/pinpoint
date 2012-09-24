package com.profiler;

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
        SystemMonitor.Worker systemMonitor = new SystemMonitor.Worker();
        systemMonitor.run();
    }

    @Test
    public void testStop() throws Exception {

    }
}
