package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.exceptiontrace.web.mapper.CLPMapper.replacePlaceHolders;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author intr3p1d
 */
class CLPMapperTest {
    private static final Logger logger = LogManager.getLogger(CLPMapperTest.class);

    @Test
    public void testEncodedLogtype() {
        String example = "INFO Task \u0011\u0000 assigned to container: [NodeAddress:\u0011\u0001, ...\n" +
                "ContainerID:\u0011\u0002], operation took \u0012\u0013 seconds";

        String replaced = replacePlaceHolders(example);
        logger.info(example);
        logger.info(replaced);
        assertNotEquals(example, replaced);
        assertFalse(replaced.contains("\u0011"));
        assertFalse(replaced.contains("\u0012"));
    }
}