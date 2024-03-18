package com.navercorp.pinpoint.plugin.fastjson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FastjsonConstantsTest {

    @Test
    public void test() {

        Assertions.assertEquals(FastjsonConstants.SCOPE, "FASTJSON_SCOPE");
        Assertions.assertEquals(FastjsonConstants.CONFIG, "profiler.json.fastjson");
        Assertions.assertEquals(FastjsonConstants.SERVICE_TYPE.getCode(), 5013);
        Assertions.assertEquals(FastjsonConstants.SERVICE_TYPE.getName(), "FASTJSON");
        Assertions.assertEquals(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getCode(), 9003);
        Assertions.assertEquals(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getName(), "fastjson.json.length");
    }
}