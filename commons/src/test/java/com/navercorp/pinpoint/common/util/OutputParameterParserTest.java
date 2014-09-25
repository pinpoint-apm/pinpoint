package com.nhn.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author emeroad
 */
public class OutputParameterParserTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OutputParameterParser parser = new OutputParameterParser();

    @Test
    public void testParseOutputParameter() throws Exception {
        assertOutputParameter("12,34", "12", "34");
        assertOutputParameter("12,,34", "12,34");

        assertOutputParameter("12,,", "12,");

        assertOutputParameter("12,,34,123", "12,34", "123");

        assertOutputParameter("12,", "12", "");

        assertOutputParameter("");

    }

    private void assertOutputParameter(String outputParam, String... params) {
        List<String> result = parser.parseOutputParameter(outputParam);
        logger.info("parseResult:{}", result);
        try {
            Assert.assertArrayEquals(result.toArray(new String[result.size()]), params);
        } catch (AssertionError e) {
            logger.warn("parseResult:{}", result);
            logger.warn("params:{}", params);
            throw e;
        }
    }
}
