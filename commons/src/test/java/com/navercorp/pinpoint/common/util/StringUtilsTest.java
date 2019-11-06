/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void abbreviate() {

        Assert.assertEquals(StringUtils.abbreviate(null), "null");
        Assert.assertEquals(StringUtils.abbreviate(null, 4), "null");
        Assert.assertEquals(StringUtils.abbreviate(null, 0), "null");
        Assert.assertEquals(StringUtils.abbreviate(null, -4), "null");

        Assert.assertEquals(StringUtils.abbreviate(longString), "This is a very long string for testing drop function. Length of ...(100)");
        Assert.assertEquals(StringUtils.abbreviate(longString, 4), "This...(100)");
        Assert.assertEquals(StringUtils.abbreviate(longString, 0), "...(100)");
        try {
            StringUtils.abbreviate(longString, -4);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assert.fail();
        }

        Assert.assertEquals(StringUtils.abbreviate(shortString), shortString);
        Assert.assertEquals(StringUtils.abbreviate(shortString, 4), "This...(22)");
        Assert.assertEquals(StringUtils.abbreviate(shortString, 0), "...(22)");
        try {
            StringUtils.abbreviate(shortString, -4);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void appendAbbreviate() {
        StringBuilder buffer = new StringBuilder();

        StringUtils.appendAbbreviate(buffer, null, 4);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, null, 0);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, null, -4);
        Assert.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, shortString, 4);
        Assert.assertEquals(buffer.toString(), "This...(22)");

        StringUtils.appendAbbreviate(buffer, longString, 16);
        Assert.assertEquals(buffer.toString(), "This...(22)This is a very l...(100)");
    }


    @Test
    public void testAbbreviate1() throws Exception {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 1);
        Assert.assertEquals("a...(3)", drop);
    }

    @Test
    public void testAbbreviate2() throws Exception {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 5);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testAbbreviate3() throws Exception {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 3);
        Assert.assertEquals("abc", drop);
    }

    @Test
    public void testAbbreviate4() throws Exception {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 0);
        Assert.assertEquals("...(3)", drop);

    }

    @Test
    public void testAbbreviateNegative() throws Exception {
        String string = "abc";
        try {
            StringUtils.abbreviate(string, -1);
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
    public void testTokenizeToStringList() throws Exception {

        final String sample = "a, b,  ,,   c";
        List<String> tokenList = StringUtils.tokenizeToStringList(sample, ",");
        Assert.assertEquals(Arrays.asList(new String[]{"a", "b", "c"}), tokenList);


    }


    @Test
    public void testTokenizeToStringList_compatibility() throws Exception {

        final String sample = "a, b,  ,,   c";
        List<String> tokenList = StringUtils.tokenizeToStringList(sample, ",");

        List<String> backup_splitAndTrim = backup_splitAndTrim(sample, ",");
        Assert.assertEquals(tokenList, backup_splitAndTrim);

    }

    @Test
    public void testTokenizeToStringList_nullValue() throws Exception {

        List<String> tokenList = StringUtils.tokenizeToStringList(null, ",");
        Assert.assertEquals(tokenList.size(), 0);

    }


    private static List<String> backup_splitAndTrim(String value, String separator) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        if (separator == null) {
            throw new NullPointerException("separator");
        }
        final List<String> result = new ArrayList<String>();
        final String[] split = value.split(separator);
        for (String method : split) {
            if (StringUtils.isEmpty(method)) {
                continue;
            }
            method = method.trim();
            if (method.isEmpty()) {
                continue;
            }
            result.add(method);
        }
        return result;
    }

    @Test
    public void testGetLength() {
        Assert.assertEquals(StringUtils.getLength(null), 0);

        Assert.assertEquals(StringUtils.getLength(""), 0);
        Assert.assertEquals(StringUtils.getLength("abc"), 3);
    }

    @Test
    public void testGetLength_defaultNull() {
        Assert.assertEquals(StringUtils.getLength(null, -1), -1);

        Assert.assertEquals(StringUtils.getLength("", -1), 0);
        Assert.assertEquals(StringUtils.getLength("abc"), 3);
    }


    @Test
    public void testHasLength() {

        Assert.assertTrue(StringUtils.hasLength("1"));
        Assert.assertTrue(StringUtils.hasLength(" "));


        Assert.assertFalse(StringUtils.hasLength(null));
        Assert.assertFalse(StringUtils.hasLength(""));
    }

    @Test
    public void testHasText() {

        Assert.assertTrue(StringUtils.hasText("1"));
        Assert.assertTrue(StringUtils.hasText("  1"));

        Assert.assertFalse(StringUtils.hasText(null));
        Assert.assertFalse(StringUtils.hasText(""));
        Assert.assertFalse(StringUtils.hasText("  "));

    }

}
