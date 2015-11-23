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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ArrayUtils;

/**
 * @author emeroad
 */
public class ArrayUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(ArrayUtilsTest.class.getName());

    @Test
    public void dropToStringSmall() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String small = ArrayUtils.dropToString(bytes, 3);
        Assert.assertEquals("[1, 2, 3, ...(1)]", small);
    }

    @Test
    public void dropToStringEqual() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String equals = ArrayUtils.dropToString(bytes, 4);
        Assert.assertEquals("[1, 2, 3, 4]", equals);

    }

    @Test
    public void dropToStringLarge() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String large = ArrayUtils.dropToString(bytes, 11);
        Assert.assertEquals("[1, 2, 3, 4]", large);

    }


    @Test
    public void dropToStringOneAndZero() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String one = ArrayUtils.dropToString(bytes, 1);
        Assert.assertEquals("[1, ...(3)]", one);

        String zero = ArrayUtils.dropToString(bytes, 0);
        Assert.assertEquals("[...(4)]", zero);
    }


    @Test
    public void dropToStringSingle() {
        byte[] bytes = new byte[] {1};

        String small = ArrayUtils.dropToString(bytes, 1);
        logger.info(small);
        Assert.assertEquals("[1]", small);
    }

    @Test
    public void dropToStringNegative() {
        byte[] bytes = new byte[] {1};

        try {
            ArrayUtils.dropToString(bytes, -1);
            Assert.fail();
        } catch (Exception ignored) {
        }
    }
}
