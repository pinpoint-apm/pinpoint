package com.navercorp.pinpoint.service.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservedServiceRegistryTest {

    private ReservedServiceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ReservedServiceRegistry();
    }

    @Test
    void contains_DEFAULT() {
        assertThat(registry.contains("DEFAULT")).isTrue();
    }

    @Test
    void contains_TEST() {
        assertThat(registry.contains("TEST")).isTrue();
    }

    @Test
    void contains_ERROR() {
        assertThat(registry.contains("ERROR")).isTrue();
    }

    @Test
    void contains_UNKNOWN() {
        assertThat(registry.contains("UNKNOWN")).isTrue();
    }

    @Test
    void contains_NULL() {
        assertThat(registry.contains("NULL")).isTrue();
    }

    @Test
    void contains_caseInsensitive() {
        assertThat(registry.contains("default")).isTrue();
        assertThat(registry.contains("Default")).isTrue();
        assertThat(registry.contains("test")).isTrue();
    }

    @Test
    void contains_nonReserved() {
        assertThat(registry.contains("my-service")).isFalse();
        assertThat(registry.contains("production")).isFalse();
    }

    @Test
    void contains_null_returnsFalse() {
        assertThat(registry.contains(null)).isFalse();
    }

    @Test
    void getReservedNames_notEmpty() {
        assertThat(registry.getReservedNames()).isNotEmpty();
        assertThat(registry.getReservedNames()).contains("DEFAULT", "TEST", "ERROR", "UNKNOWN", "NULL");
    }
}
