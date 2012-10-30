package com.profiler.util;

import org.junit.Test;

import java.util.logging.Logger;

/**
 *
 */
public class NetworkUtilsTest {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void testGetMachineName() throws Exception {
        String machineName = NetworkUtils.getMachineName();
        logger.info(machineName);
    }
}
