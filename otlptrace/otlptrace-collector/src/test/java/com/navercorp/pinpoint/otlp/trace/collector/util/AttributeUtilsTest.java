package com.navercorp.pinpoint.otlp.trace.collector.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeUtilsTest {

    // =======================================================================
    // getStringValue(Map)
    // =======================================================================

    @Test
    void getStringValue_map_found() {
        Map<String, Object> attrs = Map.of("key", "value");

        assertThat(AttributeUtils.getStringValue(attrs, "key", null)).isEqualTo("value");
    }

    @Test
    void getStringValue_map_notFound_returnsDefault() {
        Map<String, Object> attrs = Map.of("other", "value");

        assertThat(AttributeUtils.getStringValue(attrs, "key", "default")).isEqualTo("default");
    }

    @Test
    void getStringValue_map_wrongType_returnsDefault() {
        Map<String, Object> attrs = Map.of("key", 42L);

        assertThat(AttributeUtils.getStringValue(attrs, "key", "default")).isEqualTo("default");
    }

    @Test
    void getStringValue_map_nullDefault() {
        Map<String, Object> attrs = Map.of();

        assertThat(AttributeUtils.getStringValue(attrs, "key", null)).isNull();
    }

    // =======================================================================
    // getIntValue(Map)
    // =======================================================================

    @Test
    void getIntValue_map_found() {
        Map<String, Object> attrs = Map.of("count", 42L);

        assertThat(AttributeUtils.getIntValue(attrs, "count", 0L)).isEqualTo(42L);
    }

    @Test
    void getIntValue_map_notFound_returnsDefault() {
        Map<String, Object> attrs = Map.of("other", 10L);

        assertThat(AttributeUtils.getIntValue(attrs, "count", -1L)).isEqualTo(-1L);
    }

    @Test
    void getIntValue_map_wrongType_returnsDefault() {
        Map<String, Object> attrs = Map.of("count", "not-a-number");

        assertThat(AttributeUtils.getIntValue(attrs, "count", -1L)).isEqualTo(-1L);
    }

    @Test
    void getIntValue_map_longValue() {
        Map<String, Object> attrs = Map.of("port", 8080L);

        assertThat(AttributeUtils.getIntValue(attrs, "port", 0L)).isEqualTo(8080L);
    }

}