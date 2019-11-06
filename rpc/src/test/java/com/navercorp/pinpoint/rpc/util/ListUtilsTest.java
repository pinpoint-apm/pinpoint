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

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ListUtilsTest {

    @Test
    public void testAddIfValueNotNull() {
        ArrayList<String> strings = new ArrayList<String>();

        Assert.assertTrue(ListUtils.addIfValueNotNull(strings, "foo"));
        Assert.assertTrue(strings.get(0).equals("foo"));

        Assert.assertFalse(ListUtils.addIfValueNotNull(strings, null));
    }

    @Test
    public void testAddAllIfAllValuesNotNull() {
        ArrayList<String> strings = new ArrayList<String>();

        Assert.assertTrue(
                ListUtils.addAllIfAllValuesNotNull(
                        strings, new String[]{"a", "b", "c"}));
        Assert.assertTrue(strings.get(0).equals("a"));
        Assert.assertTrue(strings.get(1).equals("b"));
        Assert.assertTrue(strings.get(2).equals("c"));

        Assert.assertFalse(ListUtils.addAllIfAllValuesNotNull(strings, null));
        Assert.assertFalse(ListUtils
                .addAllIfAllValuesNotNull(strings, new String[]{null}));
    }

    @Test
    public void testGetFirst() {
        ArrayList<String> strings = new ArrayList<String>();

        Assert.assertNull(ListUtils.getFirst(null));
        Assert.assertNull(ListUtils.getFirst(strings));

        Assert.assertEquals("dValue", ListUtils.getFirst(null, "dValue"));
        Assert.assertEquals("dValue", ListUtils.getFirst(strings, "dValue"));

        strings.add("foo");
        strings.add("bar");
        strings.add("baz");

        Assert.assertEquals("foo", ListUtils.getFirst(strings));
        Assert.assertEquals("foo", ListUtils.getFirst(strings, "dValue"));
    }

    @Test
    public void testIsFirst() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("foo");
        strings.add("bar");
        strings.add("baz");

        Assert.assertTrue(ListUtils.isFirst(null, null));
        Assert.assertTrue(ListUtils.isFirst(strings, "foo"));

        Assert.assertFalse(ListUtils.isFirst(strings, "bar"));
    }

    @Test
    public void testGet() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("foo");
        strings.add("bar");
        strings.add("baz");

        Assert.assertEquals("foo", ListUtils.get(strings, 0, "dValue"));
        Assert.assertEquals("bar", ListUtils.get(strings, 1, "dValue"));
        Assert.assertEquals("dValue", ListUtils.get(null, 0, "dValue"));
    }

    @Test
    public void testGetLast() {
        ArrayList<String> strings = new ArrayList<String>();

        Assert.assertNull(ListUtils.getLast(null));
        Assert.assertNull(ListUtils.getLast(strings));

        Assert.assertEquals("dValue", ListUtils.getLast(null, "dValue"));
        Assert.assertEquals("dValue", ListUtils.getLast(strings, "dValue"));

        strings.add("foo");
        strings.add("bar");
        strings.add("baz");

        Assert.assertEquals("baz", ListUtils.getLast(strings));
        Assert.assertEquals("baz", ListUtils.getLast(strings, "dValue"));
    }

    @Test
    public void testIsLast() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("foo");
        strings.add("bar");
        strings.add("baz");

        Assert.assertTrue(ListUtils.isLast(null, null));
        Assert.assertTrue(ListUtils.isLast(strings, "baz"));

        Assert.assertFalse(ListUtils.isLast(strings, "foo"));
        Assert.assertFalse(ListUtils.isLast(strings, "bar"));
    }
}
