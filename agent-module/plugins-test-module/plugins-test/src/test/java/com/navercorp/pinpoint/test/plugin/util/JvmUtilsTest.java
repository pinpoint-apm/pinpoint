package com.navercorp.pinpoint.test.plugin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class JvmUtilsTest {

    @Test
    void isJava9() {
        Assertions.assertFalse(JvmUtils.isJava9("1.8"));

        Assertions.assertTrue(JvmUtils.isJava9("9.0"));
        Assertions.assertTrue(JvmUtils.isJava9("11.0"));
    }
}