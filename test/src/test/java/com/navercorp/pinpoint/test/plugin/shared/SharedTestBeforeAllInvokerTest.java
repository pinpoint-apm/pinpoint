package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public class SharedTestBeforeAllInvokerTest {

    @Test
    public void invoke() throws Throwable {
        SharedTestBeforeAllInvoker mock = new SharedTestBeforeAllInvoker(TestClass.class);

        List<Method> methods = mock.getMethods(TestClass.class, mock::beforeAllFilter);
        Assert.assertEquals(1, methods.size());
        Assert.assertEquals("setBeforeAllResult", methods.get(0).getName());

        mock.invoke(new Properties());
        Assert.assertNotNull(TestClass.properties);
    }

    @Test
    public void invoke_extends() throws Throwable {
        SharedTestBeforeAllInvoker mock = new SharedTestBeforeAllInvoker(ChildTestClass.class);

        List<Method> methods = mock.getMethods(ChildTestClass.class, mock::beforeAllFilter);
        Assert.assertEquals(1, methods.size());
        Assert.assertEquals("setBeforeAllResult", methods.get(0).getName());

        mock.invoke(new Properties());
        Assert.assertNotNull(ChildTestClass.properties);
    }

    public static class TestClass {
        static Properties properties;

        @SharedTestBeforeAllResult
        public static void setBeforeAllResult(Properties properties) {
            TestClass.properties = properties;
        }

        public static void fake(Properties p) {

        }
    }

    public static class ChildTestClass extends TestClass {

    }
}