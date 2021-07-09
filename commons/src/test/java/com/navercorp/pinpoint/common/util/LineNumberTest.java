package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;


public class LineNumberTest {

    @Test
    public void isNoLineNumber() {
        Assert.assertTrue(LineNumber.isNoLineNumber(0));
        Assert.assertTrue(LineNumber.isNoLineNumber(-1));
    }

    @Test
    public void isLineNumber() {
        Assert.assertTrue(LineNumber.isLineNumber(1));

        Assert.assertFalse(LineNumber.isLineNumber(0));
        Assert.assertFalse(LineNumber.isLineNumber(-1));
    }
}