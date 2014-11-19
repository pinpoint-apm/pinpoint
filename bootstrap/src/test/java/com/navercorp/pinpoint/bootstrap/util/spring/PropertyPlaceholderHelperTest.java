package com.nhn.pinpoint.bootstrap.util.spring;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class PropertyPlaceholderHelperTest {

    @Test
    public void testReplacePlaceholders() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("test", "a");

        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        String value1 = helper.replacePlaceholders("${test}", properties);
        Assert.assertEquals("a", value1);

        String value2 = helper.replacePlaceholders("123${test}456", properties);
        Assert.assertEquals("123a456", value2);

        String value3 = helper.replacePlaceholders("123${test}456${test}", properties);
        Assert.assertEquals("123a456a", value3);
    }
}