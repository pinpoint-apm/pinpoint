package com.navercorp.pinpoint.profiler.container;

import jdk.internal.platform.Metrics;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jdk.internal.platform.Container;




public class CgroupMetricTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test_cgroup_metrics(){
        Metrics metrics = Container.metrics();
        logger.debug("BlkIO: ",metrics.getBlkIOServiceCount());
    }
}
