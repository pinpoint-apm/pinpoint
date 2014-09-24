package com.nhn.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author emeroad
 */
public class PreparedStatementUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testBindSetMethod() {
        List<Method> bindVariableSetMethod = PreparedStatementUtils.findBindVariableSetMethod();
        for (Method method : bindVariableSetMethod) {
            logger.debug("{}", method);
        }
    }

    @Test
    public void testMatch() throws Exception {
        Assert.assertTrue(PreparedStatementUtils.isSetter("setNCString"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setInt"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setTestTeTst"));

    }
}
