package com.navercorp.pinpoint.plugin.fastjson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FastjsonConstantsTest {

    @Test
    public void test() {

        Assertions.assertEquals("FASTJSON_SCOPE", FastjsonConstants.SCOPE);
        Assertions.assertEquals("profiler.json.fastjson", FastjsonConstants.CONFIG);
        Assertions.assertEquals(5013, FastjsonConstants.SERVICE_TYPE.getCode());
        Assertions.assertEquals("FASTJSON", FastjsonConstants.SERVICE_TYPE.getName());
        Assertions.assertEquals(9003, FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getCode());
        Assertions.assertEquals("fastjson.json.length", FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getName());
    }
}