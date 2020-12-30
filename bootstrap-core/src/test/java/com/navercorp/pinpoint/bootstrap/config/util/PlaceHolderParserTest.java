package com.navercorp.pinpoint.bootstrap.config.util;

import org.junit.Assert;
import org.junit.Test;

public class PlaceHolderParserTest {
    private final PlaceHolderParser parser = new PlaceHolderParser();
    @Test
    public void parseFieldValue() {
        PlaceHolder placeHolder = this.parser.parse("${test}");
        Assert.assertEquals("test", placeHolder.getKey());
        Assert.assertNull(placeHolder.getDefaultValue());
    }

    @Test
    public void parseFieldValue_defaultValue() {
        PlaceHolder placeHolder = this.parser.parse("${test:100}");
        Assert.assertEquals("test", placeHolder.getKey());
        Assert.assertEquals("100", placeHolder.getDefaultValue());
    }
    @Test
    public void parseFieldValue_emptyString() {
        PlaceHolder placeHolder = this.parser.parse("${test:}");
        Assert.assertEquals("test", placeHolder.getKey());
        Assert.assertEquals("", placeHolder.getDefaultValue());
    }

    @Test
    public void parseFieldValue_emptyKey() {
        PlaceHolder placeHolder = this.parser.parse("${}");
        Assert.assertEquals("", placeHolder.getKey());
    }

    @Test
    public void parseFieldValue_null() {
        PlaceHolder placeHolder = this.parser.parse("${");
        Assert.assertNull(placeHolder);

    }
}