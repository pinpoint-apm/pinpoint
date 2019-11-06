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

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class ByteSizeUnitTest {

    private final ByteSizeUnit[] byteSizeUnitSet = EnumSet.allOf(ByteSizeUnit.class).toArray(new ByteSizeUnit[0]);

    @Test
    public void sizeTest() {
        Assert.assertTrue(ByteSizeUnit.KILO_BYTES.getUnitSize() > ByteSizeUnit.BYTES.getUnitSize());
        Assert.assertTrue(ByteSizeUnit.MEGA_BYTES.getUnitSize() > ByteSizeUnit.KILO_BYTES.getUnitSize());
        Assert.assertTrue(ByteSizeUnit.GIGA_BYTES.getUnitSize() > ByteSizeUnit.MEGA_BYTES.getUnitSize());
        Assert.assertTrue(ByteSizeUnit.TERA_BYTES.getUnitSize() > ByteSizeUnit.GIGA_BYTES.getUnitSize());
    }

    @Test
    public void byteUnitTest() {
        unitTest("13", ByteSizeUnit.BYTES);
    }

    @Test
    public void kiloUnitTest() {
        unitTest("23", ByteSizeUnit.KILO_BYTES);
    }

    @Test
    public void megaUnitTest() {
        unitTest("31", ByteSizeUnit.MEGA_BYTES);
    }

    @Test
    public void gigaUnitTest() {
        unitTest("33", ByteSizeUnit.GIGA_BYTES);
    }

    @Test
    public void teraUnitTest() {
        unitTest("91", ByteSizeUnit.TERA_BYTES);
    }

    public void unitTest(String value, ByteSizeUnit byteSizeUnit) {
        String value1 = value + byteSizeUnit.getUnitChar1();
        String value2 = value + byteSizeUnit.getUnitChar1() + ByteSizeUnit.BYTES.getUnitChar1();
        String value3 = value + byteSizeUnit.getUnitChar1() + ByteSizeUnit.BYTES.getUnitChar2();
        String value4 = value + byteSizeUnit.getUnitChar2();
        String value5 = value + byteSizeUnit.getUnitChar2() + ByteSizeUnit.BYTES.getUnitChar1();
        String value6 = value + byteSizeUnit.getUnitChar2() + ByteSizeUnit.BYTES.getUnitChar2();
        if (byteSizeUnit == ByteSizeUnit.BYTES) {
            value2 = value1;
            value3 = value1;
            value5 = value4;
            value6 = value4;
        }


        long result1 = ByteSizeUnit.getByteSize(value1);
        long result2 = ByteSizeUnit.getByteSize(value2);
        long result3 = ByteSizeUnit.getByteSize(value3);
        long result4 = ByteSizeUnit.getByteSize(value4);
        long result5 = ByteSizeUnit.getByteSize(value5);
        long result6 = ByteSizeUnit.getByteSize(value6);
        long result7 = byteSizeUnit.toBytesSize(Long.parseLong(value, 10));

        Assert.assertEquals(result1, result2);
        Assert.assertEquals(result1, result3);
        Assert.assertEquals(result1, result4);
        Assert.assertEquals(result1, result5);
        Assert.assertEquals(result1, result6);
        Assert.assertEquals(result1, result7);
    }

    @Test
    public void maxUnitTest() {
        ByteSizeUnit byteSizeUnit = getRandomByteSizeUnit(ByteSizeUnit.BYTES);

        long maxSize = byteSizeUnit.getMaxSize();
        byteSizeUnit.toBytesSize(maxSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxUnitFailTest() {
        ByteSizeUnit byteSizeUnit = getRandomByteSizeUnit(ByteSizeUnit.BYTES);

        long maxSize = byteSizeUnit.getMaxSize();
        byteSizeUnit.toBytesSize(maxSize + 1);
    }

    private ByteSizeUnit getRandomByteSizeUnit(ByteSizeUnit exceptValue) {
        Random random = new Random(System.currentTimeMillis());
        ByteSizeUnit[] values = ByteSizeUnit.values();


        while (true) {
            ByteSizeUnit value = values[random.nextInt(values.length)];
            if (exceptValue != value) {
                return value;
            }
        }
    }


    @Test
    public void expectedThrowExceptionTest() {
        expectedThrowExceptionTest("200kk");
        expectedThrowExceptionTest("200mk");
        expectedThrowExceptionTest("200gk");
        expectedThrowExceptionTest("200ggb");
        expectedThrowExceptionTest("200nb");
        expectedThrowExceptionTest("k200b");
        expectedThrowExceptionTest("200lb");
        expectedThrowExceptionTest("200Lb");
        expectedThrowExceptionTest("200Lb");
    }

    private void expectedThrowExceptionTest(String value) {
        try {
            ByteSizeUnit.getByteSize(value);
            Assert.fail(value);
        } catch (Exception e) {
        }
    }


}
