/*
 * Copyright 2014 NAVER Corp.
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
import java.util.Map;

public class CopyUtilsTest {

    @Test
    public void copyUtilsTest() {
        Map<Object, Object> original = createSimpleMap("key", 2);

        Map<Object, Object> copied = CopyUtils.mediumCopyMap(original);
        Assertions.assertEquals(2, copied.get("key"));
        Assertions.assertEquals(2, original.get("key"));

        original.put("key", 4);
        copied.put("key", 3);
        copied.put("new", "new");

        Assertions.assertEquals(3, copied.get("key"));
        Assertions.assertEquals("new", copied.get("new"));
        Assertions.assertEquals(4, original.get("key"));
    }

    @Test
    public void copyUtilsTest2() {
        Map<Object, Object> original = createSimpleMap("key", 2);

        Map<Object, Object> innerMap = createSimpleMap("innerKey", "inner");
        original.put("map", innerMap);

        Map<Object, Object> copied = CopyUtils.mediumCopyMap(original);

        Assertions.assertEquals(2, copied.get("key"));
        Assertions.assertEquals("inner", ((Map) copied.get("map")).get("innerKey"));
        Assertions.assertEquals(2, original.get("key"));
        Assertions.assertEquals("inner", ((Map) original.get("map")).get("innerKey"));

        original.put("key", 3);
        copied.put("key", 4);

        innerMap.put("innerKey", "key");
        Map<Object, Object> copiedInnerMap = (Map) copied.get("map");
        copiedInnerMap.put("test", "test");

        Assertions.assertEquals(4, copied.get("key"));
        Assertions.assertEquals("inner", ((Map) copied.get("map")).get("innerKey"));
        Assertions.assertEquals("test", ((Map) copied.get("map")).get("test"));
        Assertions.assertEquals(3, original.get("key"));
        Assertions.assertEquals("key", ((Map) original.get("map")).get("innerKey"));
        Assertions.assertFalse(((Map) original.get("map")).containsKey("test"));
    }

    private Map<Object, Object> createSimpleMap(Object key, Object value) {
        Map<Object, Object> map = new HashMap<>();
        map.put(key, value);

        return map;
    }

}
