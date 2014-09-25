package com.nhn.pinpoint.profiler.util.bindvalue;

import com.nhn.pinpoint.profiler.util.bindvalue.Types;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

public class TypesTest {

    @Test
    public void testInverse() throws Exception {
        Map<Integer, String> inverse = Types.inverse();
        Field[] fields = java.sql.Types.class.getFields();
        Assert.assertEquals(inverse.size(), fields.length);
    }
}
