package com.nhn.pinpoint.util;

import com.profiler.util.PreparedStatementUtils;
import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.List;

public class PreparedStatementUtilsTest {
    @Test
    public void testBindSetMethod() {
        List<Method> bindVariableSetMethod = PreparedStatementUtils.findBindVariableSetMethod();
        for (Method method : bindVariableSetMethod) {
            System.out.println(method);
        }
    }

    @Test
    public void testMatch() throws Exception {
        Assert.assertTrue(PreparedStatementUtils.isSetter("setNCString"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setInt"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setTestTeTst"));

    }
}
