package com.navercorp.pinpoint.web.frontend.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentalPropertiesTest {

    @Test
    void of() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("experimental.enableFeatureA.value", "true");
        environment.setProperty("experimental.enableFeatureB.value", "false");
        environment.setProperty("experimental.threshold.value", "100");
        environment.setProperty("other.property", "ignored");

        ExperimentalProperties properties = ExperimentalProperties.of(environment);
        Map<String, Object> map = properties.getProperties();

        assertThat(map).containsEntry("experimental.enableFeatureA.value", true);
        assertThat(map).containsEntry("experimental.enableFeatureB.value", false);
        assertThat(map).containsEntry("experimental.threshold.value", "100");
        assertThat(map).doesNotContainKey("other.property");
    }

    @Test
    void of_duplicateKey() {
        MockEnvironment environment = new MockEnvironment();

        // First property source (higher priority)
        MapPropertySource highPriority = new MapPropertySource("high",
                Map.of("experimental.enableFeatureA.value", "true"));
        // Second property source (lower priority)
        MapPropertySource lowPriority = new MapPropertySource("low",
                Map.of("experimental.enableFeatureA.value", "false"));

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(highPriority);
        propertySources.addLast(lowPriority);

        ExperimentalProperties properties = ExperimentalProperties.of(environment);
        Map<String, Object> map = properties.getProperties();

        // First registered value wins (higher priority source)
        assertThat(map).containsEntry("experimental.enableFeatureA.value", true);
        assertThat(map).hasSize(1);
    }

    @Test
    void of_emptyEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("other.property", "value");

        ExperimentalProperties properties = ExperimentalProperties.of(environment);

        assertThat(properties.getProperties()).isEmpty();
    }

    @Test
    void of_booleanParsing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("experimental.a.value", "TRUE");
        environment.setProperty("experimental.b.value", "False");
        environment.setProperty("experimental.c.value", "notBoolean");

        ExperimentalProperties properties = ExperimentalProperties.of(environment);
        Map<String, Object> map = properties.getProperties();

        assertThat(map.get("experimental.a.value")).isEqualTo(true);
        assertThat(map.get("experimental.b.value")).isEqualTo(false);
        assertThat(map.get("experimental.c.value")).isEqualTo("notBoolean");
    }

    @Test
    void of_nullValue() {
        MockEnvironment environment = new MockEnvironment();

        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("experimental.nullProp.value", null);
        MapPropertySource source = new MapPropertySource("nullSource", sourceMap);
        environment.getPropertySources().addFirst(source);

        ExperimentalProperties properties = ExperimentalProperties.of(environment);
        Map<String, Object> map = properties.getProperties();

        assertThat(map).containsKey("experimental.nullProp.value");
        assertThat(map.get("experimental.nullProp.value")).isNull();
    }

    @Test
    void of_emptyStringValue() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("experimental.emptyProp.value", "");

        ExperimentalProperties properties = ExperimentalProperties.of(environment);
        Map<String, Object> map = properties.getProperties();

        assertThat(map).containsEntry("experimental.emptyProp.value", "");
    }
}