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

/**
 * @author Woonduk Kang(emeroad)
 */
public class ArrayUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(ArrayUtilsTest.class.getName());

    @Test
    public void abbreviateSmall() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String small = ArrayUtils.abbreviate(bytes, 3);
        Assert.assertEquals("[1, 2, 3, ...(1)]", small);
    }

    @Test
    public void abbreviateEqual() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String equals = ArrayUtils.abbreviate(bytes, 4);
        Assert.assertEquals("[1, 2, 3, 4]", equals);

    }

    @Test
    public void abbreviateLarge() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String large = ArrayUtils.abbreviate(bytes, 11);
        Assert.assertEquals("[1, 2, 3, 4]", large);

    }


    @Test
    public void abbreviateOneAndZero() {
        byte[] bytes = new byte[]{1, 2, 3, 4};

        String one = ArrayUtils.abbreviate(bytes, 1);
        Assert.assertEquals("[1, ...(3)]", one);

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
    public void abbreviate() {
        //null test
        Assert.assertTrue(ArrayUtils.abbreviate(null).equals("null"));
        //zero-sized array test
        byte[] bytes_zero = new byte[0];
        Assert.assertEquals("[]", ArrayUtils.abbreviate(bytes_zero));
        //small buffer with default limit
        byte[] bytes_short = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes_short[i] = 'A';
        }
        Assert.assertEquals("[65, 65, 65, 65]", ArrayUtils.abbreviate(bytes_short));
        //big buffer with small limit
        byte[] bytes = new byte[256];
        for (int i = 0; i < 4; i++) {
            bytes[i] = 'A';
        }
        for (int i = 4; i < 256; i++) {
            bytes[i] = 'B';
        }
        String answer = "[";
        for (int i = 0; i < 4; i++) {
            answer = answer + "65, ";
        }
        for (int i = 4; i < 16; i++) {
            answer = answer + "66, ";
        }
        answer = answer + "...(240)]";
        Assert.assertEquals(answer, ArrayUtils.abbreviate(bytes, 16));
        //big buffer with big limit
        answer = "[";
        for (int i = 0; i < 4; i++) {
            answer = answer + "65, ";
        }
        for (int i = 4; i < 255; i++) {
            answer = answer + "66, ";
        }
        answer = answer + "66]";
        Assert.assertEquals(answer, ArrayUtils.abbreviate(bytes, 256));
        //big buffer with default limit
        answer = "[";
        for (int i = 0; i < 4; i++) {
            answer = answer + "65, ";
        }
        for (int i = 4; i < 32; i++) {
            answer = answer + "66, ";
        }
        answer = answer + "...(224)]";
        Assert.assertEquals(answer, ArrayUtils.abbreviate(bytes));
    }

}