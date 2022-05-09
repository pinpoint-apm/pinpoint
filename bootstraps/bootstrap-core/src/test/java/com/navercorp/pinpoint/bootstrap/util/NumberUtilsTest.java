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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author poap
 */
public class NumberUtilsTest {
    private final String notNumber = "H3110 W0r1d";

    @Test
    public void parseLong() {
        assertLong(Long.MIN_VALUE, 0);
        assertLong(0, 0);
        assertLong(Long.MAX_VALUE, 0);
    }

    private void assertLong(long longValue, long defaultLong) {
        Assert.assertEquals(NumberUtils.parseLong(null, defaultLong), defaultLong);
        Assert.assertEquals(NumberUtils.parseLong(String.valueOf(longValue), defaultLong), longValue);
        Assert.assertEquals(NumberUtils.parseLong(notNumber, defaultLong), defaultLong);
    }

    @Test
    public void parseInteger() {
        assertInteger(Integer.MIN_VALUE, 0);
        assertInteger(0, 0);
        assertInteger(Integer.MAX_VALUE, 0);
    }

    private void assertInteger(int integerValue, int defaultInt) {
        Assert.assertEquals(NumberUtils.parseInteger(null, defaultInt), defaultInt);
        Assert.assertEquals(NumberUtils.parseInteger(String.valueOf(integerValue), defaultInt), integerValue);
        Assert.assertEquals(NumberUtils.parseInteger(notNumber, defaultInt), defaultInt);
    }

    @Test
    public void parseShort() {
        short defaultShort = 0;

        assertShort(Short.MIN_VALUE, defaultShort);
        assertShort((short) 0, defaultShort);
        assertShort(Short.MAX_VALUE, defaultShort);
    }

    private void assertShort(short shortValue, short defaultShort) {
        Assert.assertEquals(NumberUtils.parseShort(null, shortValue), shortValue);
        Assert.assertEquals(NumberUtils.parseShort(String.valueOf(shortValue), defaultShort), shortValue);
        Assert.assertEquals(NumberUtils.parseShort(notNumber, shortValue), shortValue);
    }

    @Test
    public void toInteger() {
        short oneShort = 1;
        int oneInteger = 1;
        long oneLong = 1;
        String oneString = "1";

        Assert.assertNull(NumberUtils.toInteger(null));
        Assert.assertNull(NumberUtils.toInteger(oneShort));
        Assert.assertEquals(NumberUtils.toInteger(oneInteger), (Integer) 1);
        Assert.assertNull(NumberUtils.toInteger(oneLong));
        Assert.assertNull(NumberUtils.toInteger(oneString));
        Assert.assertNull(NumberUtils.toInteger(notNumber));
    }
}
