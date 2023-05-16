package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LineNumberTest {

    @Test
    public void isNoLineNumber() {
        Assertions.assertTrue(LineNumber.isNoLineNumber(0));
        Assertions.assertTrue(LineNumber.isNoLineNumber(-1));
    }

    @Test
    public void isLineNumber() {
        Assertions.assertTrue(LineNumber.isLineNumber(1));

        Assertions.assertFalse(LineNumber.isLineNumber(0));
        Assertions.assertFalse(LineNumber.isLineNumber(-1));
    }
}