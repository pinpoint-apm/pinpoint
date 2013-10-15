package com.nhn.pinpoint.profiler.config;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DumpTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void find() {
        DumpType none = DumpType.valueOf("NONE");
        logger.debug("type:{}", none);

        try {
            DumpType.valueOf("error");
            Assert.fail("not found");
        } catch (IllegalArgumentException e) {

        }
    }
}
