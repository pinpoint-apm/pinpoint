package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaskUtilsTest {

    @Test
    void masking() {
        String masking = MaskUtils.masking("12345", 1);
        Assertions.assertEquals("1****", masking);
    }
}