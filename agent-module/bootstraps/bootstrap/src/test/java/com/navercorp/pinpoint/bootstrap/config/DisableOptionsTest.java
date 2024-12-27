package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class DisableOptionsTest {

    @AfterEach
    void afterEach() {
        Properties properties = System.getProperties();
        properties.remove(DisableOptions.SYSTEM);
    }

    @Test
    void isBootDisabled() {
        Properties properties = System.getProperties();
        properties.setProperty(DisableOptions.SYSTEM, "true");

        Assertions.assertTrue(DisableOptions.isBootDisabled());
    }

    @Test
    void isBootDisabled_true() {
        Properties properties = System.getProperties();
        properties.setProperty(DisableOptions.SYSTEM, "false");

        Assertions.assertFalse(DisableOptions.isBootDisabled());
    }

}