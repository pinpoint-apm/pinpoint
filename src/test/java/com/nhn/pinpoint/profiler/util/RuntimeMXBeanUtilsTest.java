package com.nhn.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author emeroad
 */
public class RuntimeMXBeanUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void vmStartTime() {
        long vmStartTime = RuntimeMXBeanUtils.getVmStartTime();
        logger.debug("vmStartTime:{}", new Date(vmStartTime));
        Assert.assertNotSame(vmStartTime, 0);
    }

    @Test
    public void pid() {
        int pid = RuntimeMXBeanUtils.getPid();
        logger.debug("pid:{}", pid);
        Assert.assertTrue(pid > 0);
    }
}
