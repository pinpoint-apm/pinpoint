package com.navercorp.pinpoint.plugin.fastjson;

import org.junit.Assert;
import org.junit.Test;

public class FastjsonConstantsTest {

    @Test
    public void test() {

        Assert.assertEquals(FastjsonConstants.SCOPE, "FASTJSON_SCOPE");
        Assert.assertEquals(FastjsonConstants.CONFIG, "profiler.json.fastjson");
        Assert.assertEquals(FastjsonConstants.SERVICE_TYPE.getCode(), 5013);
        Assert.assertEquals(FastjsonConstants.SERVICE_TYPE.getName(), "FASTJSON");
        Assert.assertEquals(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getCode(), 9003);
        Assert.assertEquals(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH.getName(), "fastjson.json.length");
    }
}