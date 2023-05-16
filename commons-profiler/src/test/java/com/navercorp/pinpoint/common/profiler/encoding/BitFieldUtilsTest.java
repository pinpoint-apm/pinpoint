/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.encoding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BitFieldUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testBitField() {

        int bitField = 0;
        int position = 5;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assertions.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 1);

        bitField = -1;
        bitField = BitFieldUtils.clearBit(bitField, position);
        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 0);
    }


    @Test
    public void testGetBit_byte() {

        byte bitField = 0;
        int position = 2;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assertions.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_int() {

        int bitField = 0;
        int position = 2;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assertions.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_int_unsigned_shift() {

        int bitField = 0;
        int position = Integer.SIZE - 1;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assertions.assertTrue(BitFieldUtils.testBit(bitField, position));
        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_long_unsigned_shift() {

        long bitField = 0;
        int position = Long.SIZE - 1;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assertions.assertTrue(BitFieldUtils.testBit(bitField, position));
        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 1);


        bitField = BitFieldUtils.setBit(bitField, position, false);
        Assertions.assertFalse(BitFieldUtils.testBit(bitField, position));
        Assertions.assertEquals(BitFieldUtils.getBit(bitField, position), 0);
    }

    @Test
    public void test_2Bit() {

        int bitField = 0;
        int position = 2;

        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 0);

        bitField = set2Bit(bitField, position, true, false);

        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 1);

        bitField = set2Bit(bitField, position, false, true);
        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 2);

        bitField = set2Bit(bitField, position, true, true);

        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 3);

        bitField = -1;
        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 3);

        bitField = set2Bit(bitField, position, false, false);
        Assertions.assertEquals(BitFieldUtils.get2Bit(bitField, position), 0);
    }

    private int set2Bit(int bitField, int position, boolean first, boolean second) {
        bitField = BitFieldUtils.setBit(bitField, position, first);
        bitField = BitFieldUtils.setBit(bitField, position + 1, second);
        return bitField;
    }

}