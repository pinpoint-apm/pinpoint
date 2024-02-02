package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.navercorp.pinpoint.exceptiontrace.web.mapper.CLPMapper.makeReadableString;
import static com.navercorp.pinpoint.exceptiontrace.web.mapper.CLPMapper.replacePlaceHolders;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void testMakeReadable1() throws IOException {
        String rawExample = "getAgentsList.from: \u0011 ì\u009D´ì\u0083\u0081ì\u009D´ì\u0096´ì\u0095¼ í\u0095©ë\u008B\u0088ë\u008B¤";
        String example = "getAgentsList.from: \u0011 이상이어야 합니다";

        assertEquals(example, makeReadableString(rawExample));
    }

    @Test
    public void testMakeReadable2() {
        String rawExample = "\\n not found: limit=\u0011 content=â\u0080¦";
        String example = "\\n not found: limit=\u0011 content=…";

        assertEquals(example, makeReadableString(rawExample));
    }

    @Test
    public void testMakeReadable3() {
        String rawExample = "Request processing failed: jakarta.validation.ConstraintViolationException";
        String example = "Request processing failed: jakarta.validation.ConstraintViolationException";

        assertEquals(example, makeReadableString(rawExample));
    }
}