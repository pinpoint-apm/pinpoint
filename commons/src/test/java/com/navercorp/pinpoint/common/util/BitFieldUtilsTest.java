package com.navercorp.pinpoint.common.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Woonduk Kang(emeroad)
 */
public class BitFieldUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testBitField() {

        int bitField = 0;
        int position = 5;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assert.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 1);

        bitField = -1;
        bitField = BitFieldUtils.clearBit(bitField, position);
        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 0);
    }


    @Test
    public void testGetBit_byte() {

        byte bitField = 0;
        int position = 2;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assert.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_int() {

        int bitField = 0;
        int position = 2;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assert.assertTrue(BitFieldUtils.testBit(bitField, position));

        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_int_unsigned_shift() {

        int bitField = 0;
        int position = Integer.SIZE-1;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assert.assertTrue(BitFieldUtils.testBit(bitField, position));
        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 1);
    }

    @Test
    public void testGetBit_long_unsigned_shift() {

        long bitField = 0;
        int position = Long.SIZE -1;

        bitField = BitFieldUtils.setBit(bitField, position, true);
        Assert.assertTrue(BitFieldUtils.testBit(bitField, position));
        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 1);


        bitField = BitFieldUtils.setBit(bitField, position, false);
        Assert.assertFalse(BitFieldUtils.testBit(bitField, position));
        Assert.assertEquals(BitFieldUtils.getBit(bitField, position), 0);
    }

    @Test
    public void test_2Bit() {

        int bitField = 0;
        int position = 2;

        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 0);

        bitField = set2Bit(bitField, position, true, false);

        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 1);

        bitField = set2Bit(bitField, position, false, true);
        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 2);

        bitField = set2Bit(bitField, position, true, true);

        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 3);

        bitField = -1;
        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 3);

        bitField = set2Bit(bitField, position, false, false);
        Assert.assertEquals(BitFieldUtils.get2Bit(bitField, position), 0);
    }

    private int set2Bit(int bitField, int position, boolean first, boolean second) {
        bitField = BitFieldUtils.setBit(bitField, position, first);
        bitField = BitFieldUtils.setBit(bitField, position+1, second);
        return bitField;
    }

}