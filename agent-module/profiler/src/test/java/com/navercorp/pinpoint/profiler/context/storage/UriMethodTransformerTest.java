package com.navercorp.pinpoint.profiler.context.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UriMethodTransformerTest {

    @Test
    void transform() {
        UriMethodTransformer transformer = new UriMethodTransformer();
        Assertions.assertEquals("GET /user", transformer.transform("GET", "/user"));

        Assertions.assertEquals("/user", transformer.transform(null, "/user"));
    }
}