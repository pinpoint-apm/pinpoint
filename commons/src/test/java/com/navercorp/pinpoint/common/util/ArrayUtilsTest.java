/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArrayUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(ArrayUtilsTest.class.getName());

    @Test
    public void abbreviateSmall() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String small = ArrayUtils.abbreviate(bytes, 3);
        Assert.assertEquals("[1,2,3,...(4)]", small);
    }

    @Test
    public void abbreviateEqual() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String equals = ArrayUtils.abbreviate(bytes, 4);
        Assert.assertEquals("[1,2,3,4]", equals);

    }

    @Test
    public void abbreviateLarge() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String large = ArrayUtils.abbreviate(bytes, 11);
        Assert.assertEquals("[1,2,3,4]", large);

    }

    @Test
    public void abbreviateOneAndZero() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String one = ArrayUtils.abbreviate(bytes, 1);
        Assert.assertEquals("[1,...(4)]", one);

        String zero = ArrayUtils.abbreviate(bytes, 0);
        Assert.assertEquals("[...(4)]", zero);
    }


    @Test
    public void abbreviateSingle() {
        byte[] bytes = new byte[]{1};

        String small = ArrayUtils.abbreviate(bytes, 1);
        logger.debug(small);
        Assert.assertEquals("[1]", small);
    }

    @Test
    public void abbreviateNegative() {
        byte[] bytes = new byte[]{1};

        try {
            ArrayUtils.abbreviate(bytes, -1);
            Assert.fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void abbreviate_null_empty() {
        //null test
        Assert.assertEquals("null", ArrayUtils.abbreviate(null));
        //zero-sized array test
        byte[] empty = new byte[0];
        Assert.assertEquals("[]", ArrayUtils.abbreviate(empty));
        Assert.assertEquals("[]", ArrayUtils.abbreviate(empty, 0));
    }

    @Test
    public void abbreviate_simple2() {
        final byte A = 'A';
        //small buffer with default limit
        byte[] bytes_short = new byte[4];
        Arrays.fill(bytes_short, 0, 4, A);
        Assert.assertEquals("[65,65,65,65]", ArrayUtils.abbreviate(bytes_short));
    }

    @Test
    public void abbreviate() {
        final byte A = 'A';
        final byte B = 'B';

        // big buffer with small limit
        byte[] bytes = new byte[256];
        Arrays.fill(bytes, 0, 4, A);
        Arrays.fill(bytes, 4, 256, B);

        String smallStr = fill(",", A, 4, B, 16 - 4);
        String smallAnswer = "[" + smallStr + ",...(256)]";
        Assert.assertEquals(smallAnswer, ArrayUtils.abbreviate(bytes, 16));

        // big buffer with big limit
        String bigStr = fill(",", A, 4, B, 256 - 4);
        String bigAnswer = "[" + bigStr + "]";
        Assert.assertEquals(bigAnswer, ArrayUtils.abbreviate(bytes, 256));

        // big buffer with default limit
        String bitStrLimit = fill(",", A, 4, B, 32 - 4);
        String bigAnswerLimit = "[" + bitStrLimit + ",...(256)]";
        Assert.assertEquals(bigAnswerLimit, ArrayUtils.abbreviate(bytes));
    }

    private String fill(String delimiter, byte byte1, int repeat1, byte byte2, int repeat2) {
        final String str1 = String.valueOf(byte1);
        final String str2 = String.valueOf(byte2);

        StringJoiner stringJoiner = new StringJoiner(delimiter);
        fill(stringJoiner, str1, repeat1);
        fill(stringJoiner, str2, repeat2);
        return stringJoiner.toString();
    }

    private void fill(StringJoiner stringJoiner, String str, int repeat) {
        for (int i = 0; i < repeat; i++) {
            stringJoiner.add(str);
        }
    }

    @Test
    public void abbreviateBufferSize_simple1() {
        byte[] bytes = new byte[2];
        int expected = "[1,1]".length();
        Assert.assertEquals(expected, ArrayUtils.abbreviateBufferSize(bytes, 2));
    }

    @Test
    public void abbreviateBufferSize_simple2() {
        byte[] bytes = new byte[]{0, 1, 64, 127};
        int expected = "[0,1,64,127]".length();
        Assert.assertEquals(expected, ArrayUtils.abbreviateBufferSize(bytes, 5));
    }

    @Test
    public void abbreviateBufferSize_abbreviate1() {
        byte[] bytes = new byte[128];
        int expected = "[1,2,3,...(128)]".length();
        Assert.assertEquals(expected, ArrayUtils.abbreviateBufferSize(bytes, 3));
    }

    @Test
    public void abbreviateBufferSize_abbreviate2() {
        byte[] bytes = new byte[2];
        int expected = "[...(2)]".length();
        Assert.assertEquals(expected, ArrayUtils.abbreviateBufferSize(bytes, 0));
    }

}