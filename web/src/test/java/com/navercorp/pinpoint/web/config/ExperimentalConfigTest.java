package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.web.frontend.config.ExperimentalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ExperimentalConfigTest {

    @Test
    public void getProperties_filter() {
        MockEnvironment environment = new MockEnvironment();
        String key = ExperimentalProperties.PREFIX + "test";
        environment.setProperty(key, "strValue");
        environment.setProperty("ignore.test", "aabbcc");

        ExperimentalProperties config = ExperimentalProperties.of(environment);

        Map<String, Object> map = config.getProperties();

        assertThat(map)
                .hasSize(1)
                .containsEntry(key, "strValue");

    }

    @Test
    public void getProperties_boolean() {
        MockEnvironment environment = new MockEnvironment();

        String falseKey = ExperimentalProperties.PREFIX + "boolean.false";
        environment.setProperty(falseKey, "false");

        String trueKey = ExperimentalProperties.PREFIX + "boolean.true";
        environment.setProperty(trueKey, "true");


        ExperimentalProperties config = ExperimentalProperties.of(environment);
        Map<String, Object> map = config.getProperties();

        assertThat(map)
                .containsEntry(falseKey, false)
                .containsEntry(trueKey, true);
    }


}