package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AgentVersionPostfixTest {

    @Test
    void isStableVersion() {
        Assertions.assertTrue(AgentVersionPostfix.isStableVersion("1.0.0"));
        Assertions.assertTrue(AgentVersionPostfix.isStableVersion("1.0.0-p1"));

        Assertions.assertFalse(AgentVersionPostfix.isStableVersion("1.0.0-RC1"));
        Assertions.assertFalse(AgentVersionPostfix.isStableVersion("1.0.0-SNAPSHOT"));
    }
}