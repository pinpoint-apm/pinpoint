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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author poap
 */
public class StringUtilsTest {
    private final String longString = "This is a very long string for testing drop function. Length of this string is more than sixty four.";
    private final String shortString = "This is ashort string.";

    @Test
    public void defaultString() {
        Assertions.assertNull(StringUtils.defaultString(null, null));
        Assertions.assertEquals(StringUtils.defaultString(null, shortString), shortString);
        Assertions.assertEquals(StringUtils.defaultString(shortString, null), shortString);
        Assertions.assertEquals(StringUtils.defaultString(shortString, shortString), shortString);
    }

    @Test
    public void defaultIfEmpty() {
        Assertions.assertNull(StringUtils.defaultIfEmpty("", null));
        Assertions.assertEquals(StringUtils.defaultIfEmpty("", shortString), shortString);
        Assertions.assertEquals(StringUtils.defaultIfEmpty(shortString, null), shortString);
        Assertions.assertEquals(StringUtils.defaultIfEmpty(shortString, shortString), shortString);
    }


    @Test
    public void toStringTest() {
        int[] array = {0, 1};

        Assertions.assertEquals(StringUtils.toString(null), "null");
        Assertions.assertEquals(StringUtils.toString(1), "1");
        Assertions.assertEquals(StringUtils.toString(1234.567), "1234.567");
        Assertions.assertEquals(StringUtils.toString(shortString), shortString);
        Assertions.assertEquals(StringUtils.toString(array), array.toString());
    }

    @Test
    public void abbreviate() {

        Assertions.assertEquals(StringUtils.abbreviate(null), "null");
        Assertions.assertEquals(StringUtils.abbreviate(null, 4), "null");
        Assertions.assertEquals(StringUtils.abbreviate(null, 0), "null");
        Assertions.assertEquals(StringUtils.abbreviate(null, -4), "null");

        Assertions.assertEquals(StringUtils.abbreviate(longString), "This is a very long string for testing drop function. Length of ...(100)");
        Assertions.assertEquals(StringUtils.abbreviate(longString, 4), "This...(100)");
        Assertions.assertEquals(StringUtils.abbreviate(longString, 0), "...(100)");
        try {
            StringUtils.abbreviate(longString, -4);
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assertions.fail();
        }

        Assertions.assertEquals(StringUtils.abbreviate(shortString), shortString);
        Assertions.assertEquals(StringUtils.abbreviate(shortString, 4), "This...(22)");
        Assertions.assertEquals(StringUtils.abbreviate(shortString, 0), "...(22)");
        try {
            StringUtils.abbreviate(shortString, -4);
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void appendAbbreviate() {
        StringBuilder buffer = new StringBuilder();

        StringUtils.appendAbbreviate(buffer, null, 4);
        Assertions.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, null, 0);
        Assertions.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, null, -4);
        Assertions.assertEquals(buffer.toString(), "");

        StringUtils.appendAbbreviate(buffer, shortString, 4);
        Assertions.assertEquals(buffer.toString(), "This...(22)");

        StringUtils.appendAbbreviate(buffer, longString, 16);
        Assertions.assertEquals(buffer.toString(), "This...(22)This is a very l...(100)");
    }


    @Test
    public void testAbbreviate1() {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 1);
        Assertions.assertEquals("a...(3)", drop);
    }

    @Test
    public void testAbbreviate2() {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 5);
        Assertions.assertEquals("abc", drop);
    }

    @Test
    public void testAbbreviate3() {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 3);
        Assertions.assertEquals("abc", drop);
    }

    @Test
    public void testAbbreviate4() {
        String string = "abc";
        String drop = StringUtils.abbreviate(string, 0);
        Assertions.assertEquals("...(3)", drop);

    }

    @Test
    public void testAbbreviateNegative() {
        String string = "abc";
        try {
            StringUtils.abbreviate(string, -1);
            Assertions.fail();
        } catch (Exception ignored) {
            // skip
        }
    }

    @Test
    public void testIsEmpty() {
        Assertions.assertTrue(StringUtils.isEmpty(""));
        Assertions.assertTrue(StringUtils.isEmpty(null));

        Assertions.assertFalse(StringUtils.isEmpty("a"));
    }

    @Test
    public void testTokenizeToStringList() {

        final String sample = "a, b,  ,,   c";
        List<String> tokenList = StringUtils.tokenizeToStringList(sample, ",");
        Assertions.assertEquals(Arrays.asList("a", "b", "c"), tokenList);


    }


    @Test
    public void testTokenizeToStringList_compatibility() {

        final String sample = "a, b,  ,,   c";
        List<String> tokenList = StringUtils.tokenizeToStringList(sample, ",");

        List<String> backup_splitAndTrim = backup_splitAndTrim(sample, ",");
        Assertions.assertEquals(tokenList, backup_splitAndTrim);

    }

    @Test
    public void testTokenizeToStringList_nullValue() {

        List<String> tokenList = StringUtils.tokenizeToStringList(null, ",");
        Assertions.assertEquals(tokenList.size(), 0);

    }


    private static List<String> backup_splitAndTrim(String value, String separator) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }

        Objects.requireNonNull(separator, "separator");

        final List<String> result = new ArrayList<>();
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
        Assertions.assertEquals(StringUtils.getLength(null), 0);

        Assertions.assertEquals(StringUtils.getLength(""), 0);
        Assertions.assertEquals(StringUtils.getLength("abc"), 3);
    }

    @Test
    public void testGetLength_defaultNull() {
        Assertions.assertEquals(StringUtils.getLength(null, -1), -1);

        Assertions.assertEquals(StringUtils.getLength("", -1), 0);
        Assertions.assertEquals(StringUtils.getLength("abc"), 3);
    }


    @Test
    public void testHasLength() {

        Assertions.assertTrue(StringUtils.hasLength("1"));
        Assertions.assertTrue(StringUtils.hasLength(" "));


        Assertions.assertFalse(StringUtils.hasLength(null));
        Assertions.assertFalse(StringUtils.hasLength(""));
    }

    @Test
    public void testHasText() {

        Assertions.assertTrue(StringUtils.hasText("1"));
        Assertions.assertTrue(StringUtils.hasText("  1"));

        Assertions.assertFalse(StringUtils.hasText(null));
        Assertions.assertFalse(StringUtils.hasText(""));
        Assertions.assertFalse(StringUtils.hasText("  "));

    }

    @Test
    public void abbreviateBufferSize() {
        Assertions.assertEquals("a...(123)".length(), StringUtils.abbreviateBufferSize(1, 123));
    }


}
