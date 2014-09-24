package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Header;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HeaderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testToString() throws Exception {
        logger.debug("{}", Header.HTTP_FLAGS);
    }
}
