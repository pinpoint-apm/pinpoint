package com.nhn.pinpoint.interceptor.bci;

import org.junit.Assert;
import org.junit.Test;

public class CodeBuilderTest {
    @Test
    public void testCodeBuilder() throws Exception {
        CodeBuilder builder = new CodeBuilder();
        builder.begin();
        builder.format("1");
        builder.end();
        Assert.assertEquals("{1}", builder.toString());
    }

    @Test
    public void testFormat() throws Exception {
        CodeBuilder builder = new CodeBuilder();
        builder.begin();
        builder.format("1");
        builder.format("2");
        builder.end();
        Assert.assertEquals("{12}", builder.toString());
    }


}
