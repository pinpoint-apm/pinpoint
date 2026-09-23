package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HandlerIdParserTest {

    private final HandlerIdParser parser = new HandlerIdParser();

    @Test
    public void parse() {
        String key = OptionKey.getKey("handlerId", OptionKey.CLASSNAME);
        assertEquals("handlerId", parser.parse(key));
    }

    @Test
    public void parse_exceptionMessageOption() {
        String key = OptionKey.getExceptionMessageContains("my-handler_1");
        assertEquals("my-handler_1", parser.parse(key));
    }

    @Test
    public void parse_notAnOptionKey() {
        assertNull(parser.parse("profiler.sampling.rate"));
    }
}
