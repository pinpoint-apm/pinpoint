package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Properties;

public class PropertyUtilsTest  {

    @Test
    public void testLoadProperty() throws Exception {
        URL resource = PropertyUtils.class.getClassLoader().getResource("test.properties");
        String path = resource.getPath();

        Properties properties = PropertyUtils.loadProperty(path);
        assertProperty(properties);
    }

    @Test
    public void testLoadPropertyFromClassPath() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test.properties");
        assertProperty(properties);
    }

    private void assertProperty(Properties properties) {
        String test = properties.getProperty("test");
        Assert.assertEquals("pinpoint", test);
    }
}