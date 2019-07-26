/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.rpc.util;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class MapUtilsTest {

    @Test
    public void testGetString() {
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put(1, "333");

        Assert.assertNull(MapUtils.getString(null, "foo"));
        Assert.assertNull(MapUtils.getString(hashMap, "1"));

        Assert.assertEquals("222", MapUtils.getString(hashMap, "bar"));
        Assert.assertEquals("222",
                MapUtils.getString(hashMap, "bar", "dValue"));
        Assert.assertEquals("dValue",
                MapUtils.getString(null, "foo", "dValue"));
        Assert.assertEquals("dValue",
                MapUtils.getString(hashMap, "1", "dValue"));
    }

    @Test
    public void testGetBoolean() {
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("foo", true);
        hashMap.put("bar", "111");

        Assert.assertTrue(MapUtils.getBoolean(hashMap, "foo"));
        Assert.assertTrue(MapUtils.getBoolean(hashMap, "foo", true));
        Assert.assertTrue(MapUtils.getBoolean(null, "foo", true));
        Assert.assertTrue(MapUtils.getBoolean(hashMap, "bar", true));

        Assert.assertFalse(MapUtils.getBoolean(null, "foo"));
        Assert.assertFalse(MapUtils.getBoolean(hashMap, "bar"));
    }

    @Test
    public void testGetInteger() {
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put("baz", 333);

        Assert.assertNull(MapUtils.getInteger(null, "foo"));
        Assert.assertNull(MapUtils.getInteger(hashMap, "bar"));

        Assert.assertEquals(new Integer(333),
                MapUtils.getInteger(hashMap, "baz"));
        Assert.assertEquals(new Integer(333),
                MapUtils.getInteger(hashMap, "baz", 88));
        Assert.assertEquals(new Integer(88),
                MapUtils.getInteger(null, "foo", 88));
        Assert.assertEquals(new Integer(88),
                MapUtils.getInteger(hashMap, "bar", 88));
    }

    @Test
    public void testGetLong() {
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put("baz", 2L);

        Assert.assertNull(MapUtils.getLong(null, "foo"));
        Assert.assertNull(MapUtils.getLong(hashMap, "bar"));

        Assert.assertEquals(2L, MapUtils.getLong(hashMap, "baz"), 0);
        Assert.assertEquals(2L, MapUtils.getLong(hashMap, "baz", 88L), 0);
        Assert.assertEquals(88L, MapUtils.getLong(null, "foo", 88L), 0);
        Assert.assertEquals(88L, MapUtils.getLong(hashMap, "bar", 88L), 0);
    }
}
