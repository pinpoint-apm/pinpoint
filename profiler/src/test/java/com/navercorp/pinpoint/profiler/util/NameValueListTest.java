/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author poap
 */
public class NameValueListTest {
    private NameValueList<Integer> list;

    @BeforeEach
    public void beforeTest() {
        list = new NameValueList<>();
        list.add("one", 1);
        list.add("two", 2);
        list.add("three", 3);
    }

    @Test
    public void add() {
        Assertions.assertEquals(list.add("one", 11).intValue(), 1);
        Assertions.assertEquals(list.add("two", 22).intValue(), 2);
        Assertions.assertEquals(list.add("three", 33).intValue(), 3);
        Assertions.assertNull(list.add("four", 4));
        Assertions.assertEquals(list.add("one", 111).intValue(), 11);
        Assertions.assertEquals(list.add("two", 222).intValue(), 22);
        Assertions.assertEquals(list.add("three", 333).intValue(), 33);
        Assertions.assertEquals(list.add("four", 44).intValue(), 4);
        Assertions.assertNull(list.add("five", 5));
    }

    @Test
    public void get() {
        Assertions.assertEquals(list.get("one").intValue(), 1);
        Assertions.assertEquals(list.get("two").intValue(), 2);
        Assertions.assertEquals(list.get("three").intValue(), 3);
        Assertions.assertNull(list.get("four"));
    }

    @Test
    public void remove() {
        Assertions.assertEquals(list.remove("one").intValue(), 1);
        Assertions.assertEquals(list.remove("two").intValue(), 2);
        Assertions.assertEquals(list.remove("three").intValue(), 3);
        Assertions.assertNull(list.remove("four"));
        Assertions.assertNull(list.remove("three"));
        Assertions.assertNull(list.remove("two"));
        Assertions.assertNull(list.remove("four"));
    }

    @Test
    public void clear() {
        list.clear();
        Assertions.assertNull(list.get("one"));
        Assertions.assertNull(list.get("two"));
        Assertions.assertNull(list.get("three"));
    }
}
