package com.navercorp.pinpoint.bootstrap.java9.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleUtilsTest {

    @Test
    void isJava11JvmVersionUpper() {
        assertFalse(ModuleUtils.jvmVersionUpper(7));
        assertFalse(ModuleUtils.jvmVersionUpper(8));

        assertTrue(ModuleUtils.jvmVersionUpper(11));
        assertTrue(ModuleUtils.jvmVersionUpper(17));
    }
}