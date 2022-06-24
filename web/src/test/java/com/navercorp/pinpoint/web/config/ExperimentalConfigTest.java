package com.navercorp.pinpoint.web.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

public class ExperimentalConfigTest {

    @Test
    public void getProperties_filter() {
        MockEnvironment environment = new MockEnvironment();
        String key = ExperimentalConfig.PREFIX + "test";
        environment.setProperty(key, "strValue");
        environment.setProperty("ignore.test", "aabbcc");

        ExperimentalConfig config = new ExperimentalConfig(environment);

        Map<String, Object> map = config.getProperties();

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals("strValue", map.get(key));
    }

    @Test
    public void getProperties_boolean() {
        MockEnvironment environment = new MockEnvironment();

        String falseKey = ExperimentalConfig.PREFIX + "boolean.false";
        environment.setProperty(falseKey, "false");

        String trueKey = ExperimentalConfig.PREFIX + "boolean.true";
        environment.setProperty(trueKey, "true");


        ExperimentalConfig config = new ExperimentalConfig(environment);
        Map<String, Object> map = config.getProperties();


        Assertions.assertEquals(Boolean.FALSE, map.get(falseKey));
        Assertions.assertEquals(Boolean.TRUE, map.get(trueKey));
    }


}