package com.navercorp.pinpoint.tools.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class PropertyResolverTest {

    @Test
    void resolve() {
        Properties properties = new Properties();
        properties.setProperty("reference.value", "${value1}");
        properties.setProperty("value1", "1");

        PropertyResolver resolver = new PropertyResolver(properties);

        String refValue = resolver.resolve("reference.value");
        Assertions.assertEquals("1", refValue);

        String value = resolver.resolve("value1");
        Assertions.assertEquals("1", value);
    }

    @Test
    void resolve2() {
        Properties properties = new Properties();
        properties.setProperty("reference.value1", "${reference.value2}");
        properties.setProperty("reference.value2", "${value1}");
        properties.setProperty("value1", "1");

        PropertyResolver resolver = new PropertyResolver(properties);

        String refValue = resolver.resolve("reference.value1");
        Assertions.assertEquals("1", refValue);

    }
}