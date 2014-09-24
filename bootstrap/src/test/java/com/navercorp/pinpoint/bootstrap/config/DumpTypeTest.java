package com.nhn.pinpoint.bootstrap.config;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DumpTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void find() {
        DumpType none = DumpType.valueOf("ALWAYS");
        logger.debug("type:{}", none);

        try {
            DumpType.valueOf("error");
            Assert.fail("not found");
        } catch (IllegalArgumentException e) {

        }
    }
}
