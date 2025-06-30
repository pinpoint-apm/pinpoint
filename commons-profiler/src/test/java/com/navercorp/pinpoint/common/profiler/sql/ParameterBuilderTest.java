package com.navercorp.pinpoint.common.profiler.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterBuilderTest {

    @Test
    void isChange1() {
        ParameterBuilder parameterBuilder = new ParameterBuilder();
        assertFalse(parameterBuilder.isChange());

        parameterBuilder.append("test");
        assertTrue(parameterBuilder.isChange());

    }

    @Test
    void touch() {
        ParameterBuilder parameterBuilder = new ParameterBuilder();

        parameterBuilder.touch();
        assertTrue(parameterBuilder.isChange());
    }
}