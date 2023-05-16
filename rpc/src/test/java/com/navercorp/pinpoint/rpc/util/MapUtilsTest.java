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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class MapUtilsTest {

    @Test
    public void testGetString() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put(1, "333");

        Assertions.assertNull(MapUtils.getString(null, "foo"));
        Assertions.assertNull(MapUtils.getString(hashMap, "1"));

        Assertions.assertEquals("222", MapUtils.getString(hashMap, "bar"));
        Assertions.assertEquals("222",
                MapUtils.getString(hashMap, "bar", "dValue"));
        Assertions.assertEquals("dValue",
                MapUtils.getString(null, "foo", "dValue"));
        Assertions.assertEquals("dValue",
                MapUtils.getString(hashMap, "1", "dValue"));
    }

    @Test
    public void testGetBoolean() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("foo", true);
        hashMap.put("bar", "111");

        Assertions.assertTrue(MapUtils.getBoolean(hashMap, "foo"));
        Assertions.assertTrue(MapUtils.getBoolean(hashMap, "foo", true));
        Assertions.assertTrue(MapUtils.getBoolean(null, "foo", true));
        Assertions.assertTrue(MapUtils.getBoolean(hashMap, "bar", true));

        Assertions.assertFalse(MapUtils.getBoolean(null, "foo"));
        Assertions.assertFalse(MapUtils.getBoolean(hashMap, "bar"));
    }

    @Test
    public void testGetInteger() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put("baz", 333);

        Assertions.assertNull(MapUtils.getInteger(null, "foo"));
        Assertions.assertNull(MapUtils.getInteger(hashMap, "bar"));

        Assertions.assertEquals(new Integer(333),
                MapUtils.getInteger(hashMap, "baz"));
        Assertions.assertEquals(new Integer(333),
                MapUtils.getInteger(hashMap, "baz", 88));
        Assertions.assertEquals(new Integer(88),
                MapUtils.getInteger(null, "foo", 88));
        Assertions.assertEquals(new Integer(88),
                MapUtils.getInteger(hashMap, "bar", 88));
    }

    @Test
    public void testGetLong() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("foo", "111");
        hashMap.put("bar", "222");
        hashMap.put("baz", 2L);

        Assertions.assertNull(MapUtils.getLong(null, "foo"));
        Assertions.assertNull(MapUtils.getLong(hashMap, "bar"));

        Assertions.assertEquals(2L, MapUtils.getLong(hashMap, "baz"), 0);
        Assertions.assertEquals(2L, MapUtils.getLong(hashMap, "baz", 88L), 0);
        Assertions.assertEquals(88L, MapUtils.getLong(null, "foo", 88L), 0);
        Assertions.assertEquals(88L, MapUtils.getLong(hashMap, "bar", 88L), 0);
    }
}
