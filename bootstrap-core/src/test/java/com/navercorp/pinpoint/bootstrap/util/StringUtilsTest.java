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

package com.navercorp.pinpoint.bootstrap.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author poap
 */
public class StringUtilsTest {
    private String longString = "This is a very long string for testing drop function. Length of this string is more than sixty four.";
    private String shortString = "This is ashort string.";

    @Test
    public void defaultString() {
        Assert.assertNull(StringUtils.defaultString(null, null));
        Assert.assertEquals(StringUtils.defaultString(null, shortString), shortString);
        Assert.assertEquals(StringUtils.defaultString(shortString, null), shortString);
        Assert.assertEquals(StringUtils.defaultString(shortString, shortString), shortString);
    }

    @Test
    public void toStringTest() {
        int array[] = {0, 1};

        Assert.assertEquals(StringUtils.toString(null), "null");
        Assert.assertEquals(StringUtils.toString(1), "1");
        Assert.assertEquals(StringUtils.toString(1234.567), "1234.567");
        Assert.assertEquals(StringUtils.toString(shortString), shortString);
        Assert.assertEquals(StringUtils.toString(array), array.toString());
    }

    @Test
    public void drop() {

        Assert.assertEquals(StringUtils.drop(null), "null");
        Assert.assertEquals(StringUtils.drop(null, 4), "null");
        Assert.assertEquals(StringUtils.drop(null, 0), "null");
        Assert.assertEquals(StringUtils.drop(null, -4), "null");

        Assert.assertEquals(StringUtils.drop(longString), "This is a very long string for testing drop function. Length of ...(100)");
        Assert.assertEquals(StringUtils.drop(longString, 4), "This...(100)");
        Assert.assertEquals(StringUtils.drop(longString, 0), "...(100)");
        try {
            StringUtils.drop(longString, -4);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertEquals(StringUtils.drop(shortString), shortString);
        Assert.assertEquals(StringUtils.drop(shortString, 4), "This...(22)");
        Assert.assertEquals(StringUtils.drop(shortString, 0), "...(22)");
        try {
            StringUtils.drop(shortString, -4);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void appendDrop() {
        StringBuilder buffer = new StringBuilder();

        StringUtils.appendDrop(buffer, null, 4);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendDrop(buffer, null, 0);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendDrop(buffer, null, -4);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendDrop(buffer, shortString, 4);
        Assert.assertEquals(buffer.toString(), "This...(22)");

        StringUtils.appendDrop(buffer, longString, 16);
        Assert.assertEquals(buffer.toString(), "This...(22)This is a very l...(100)");
    }


    @Test
    public void testDrop1() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 1);
        Assert.assertEquals("a...(3)", drop);
    }

    @Test
    public void testDrop2() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 5);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testDrop3() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 3);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testDrop4() throws Exception {
        String string = "abc";
        String drop = StringUtils.drop(string, 0);
        Assert.assertEquals("...(3)", drop);

    }

    @Test
    public void testDropNegative() throws Exception {
        String string = "abc";
        try {
            StringUtils.drop(string, -1);
            Assert.fail();
        } catch (Exception ignore) {
            // skip
        }
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty(null));

        Assert.assertFalse(StringUtils.isEmpty("a"));
    }

    @Test
    public void testSplitAndTrim() throws Exception {

        List<String> strings = StringUtils.splitAndTrim("a, b,  ,,   c", ",");
        Assert.assertEquals(Arrays.asList(new String[]{"a", "b", "c"}), strings);

    }


}
