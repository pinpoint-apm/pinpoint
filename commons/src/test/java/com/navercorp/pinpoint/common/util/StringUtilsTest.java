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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author poap
 */
public class StringUtilsTest {
    private final String longString = "This is a very long string for testing drop function. Length of this string is more than sixty four.";
    private final String shortString = "This is ashort string.";

    @Test
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    public void toStringTest() {
        int[] array = {0, 1};

        Assertions.assertEquals("null", StringUtils.toString(null));
        Assertions.assertEquals("1", StringUtils.toString(1));
        Assertions.assertEquals("1234.567", StringUtils.toString(1234.567));
        Assertions.assertEquals(shortString, StringUtils.toString(shortString));
        Assertions.assertEquals(array.toString(), StringUtils.toString(array));
    }

    @Test
    public void abbreviate() {

        Assertions.assertEquals("null", StringUtils.abbreviate(null));
        Assertions.assertEquals("null", StringUtils.abbreviate(null, 4));
        Assertions.assertEquals("null", StringUtils.abbreviate(null, 0));
        Assertions.assertEquals("null", StringUtils.abbreviate(null, -4));

        Assertions.assertEquals("This is a very long string for testing drop function. Length of ...(100)", StringUtils.abbreviate(longString));
        Assertions.assertEquals("This...(100)", StringUtils.abbreviate(longString, 4));
        Assertions.assertEquals("...(100)", StringUtils.abbreviate(longString, 0));

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            StringUtils.abbreviate(longString, -4);
        });

        Assertions.assertEquals(shortString, StringUtils.abbreviate(shortString));
        Assertions.assertEquals("This...(22)", StringUtils.abbreviate(shortString, 4));
        Assertions.assertEquals("...(22)", StringUtils.abbreviate(shortString, 0));

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            StringUtils.abbreviate(shortString, -4);
        });
    }

    @Test
    public void appendAbbreviate() {
        StringBuilder buffer = new StringBuilder();

        StringUtils.appendAbbreviate(buffer, null, 4);
        Assertions.assertEquals("", buffer.toString());

        StringUtils.appendAbbreviate(buffer, null, 0);
        Assertions.assertEquals("", buffer.toString());

        StringUtils.appendAbbreviate(buffer, null, -4);
        Assertions.assertEquals("", buffer.toString());

        StringUtils.appendAbbreviate(buffer, shortString, 4);
        Assertions.assertEquals("This...(22)", buffer.toString());

        StringUtils.appendAbbreviate(buffer, longString, 16);
        Assertions.assertEquals("This...(22)This is a very l...(100)", buffer.toString());
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
        assertThat(tokenList).isEmpty();
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
        Assertions.assertEquals(0, StringUtils.getLength(null));

        Assertions.assertEquals(0, StringUtils.getLength(""));
        Assertions.assertEquals(3, StringUtils.getLength("abc"));
    }

    @Test
    public void testGetLength_defaultNull() {
        Assertions.assertEquals(-1, StringUtils.getLength(null, -1));

        Assertions.assertEquals(0, StringUtils.getLength("", -1));
        Assertions.assertEquals(3, StringUtils.getLength("abc"));
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


    @Test
    void trim() {
        Assertions.assertEquals("a", StringUtils.trim(" a "));
        Assertions.assertNull(StringUtils.trim(null));
    }

    @Test
    void trimToEmpty() {
        Assertions.assertEquals("a", StringUtils.trimToEmpty(" a "));
        Assertions.assertEquals("", StringUtils.trimToEmpty(null));
    }

    @Test
    public void testRightTrim() {
        assertEquals("test", StringUtils.rightTrim("test  "));
        assertEquals("test", StringUtils.rightTrim("test"));
        assertEquals("  test", StringUtils.rightTrim("  test"));

    }

    @Test
    public void testRightTrim2() {
        // no space
        String testStr1 = "0123456789 abc";
        assertEquals("0123456789 abc", StringUtils.rightTrim(testStr1));
        // right spaced
        String testStr2 = "0123456789 abcabc!       ";
        assertEquals("0123456789 abcabc!", StringUtils.rightTrim(testStr2));
    }

    @Test
    public void toStringAndRightTrim_empty() {
        Assertions.assertEquals("", StringUtils.rightTrim(""));
        Assertions.assertEquals("", StringUtils.rightTrim(" "));
        Assertions.assertEquals("", StringUtils.rightTrim("  "));
        Assertions.assertEquals("", StringUtils.rightTrim("     "));
    }

    @Test
    public void toStringAndRightTrim() {
        Assertions.assertEquals("1", StringUtils.rightTrim("1"));
        Assertions.assertEquals("2", StringUtils.rightTrim("2 "));
        Assertions.assertEquals("3", StringUtils.rightTrim("3  "));
        Assertions.assertEquals("4", StringUtils.rightTrim("4     "));

        Assertions.assertEquals("5 1", StringUtils.rightTrim("5 1 "));
    }

    @Test
    public void toStringAndRightTrimToEmpty() {
        Assertions.assertEquals("", StringUtils.rightTrimToEmpty(null));
    }
}
