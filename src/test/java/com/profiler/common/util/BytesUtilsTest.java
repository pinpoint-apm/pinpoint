package com.profiler.common.util;

import java.util.Arrays;
import java.util.UUID;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;

public class BytesUtilsTest {
    @Test
    public void testLongLongToBytes() throws Exception {
        long most = Long.MAX_VALUE;
        long least = Long.MAX_VALUE - 1;

        test(most, least);

        UUID uuid = UUID.randomUUID();
        test(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    @Test
    public void testInt() {
        int i = Integer.MAX_VALUE - 5;
        checkInt(i);
        checkInt(23464);
    }

    private void checkInt(int i) {
        byte[] bytes = Bytes.toBytes(i);
        int i2 = BytesUtils.bytesToInt(bytes, 0);
        Assert.assertEquals(i, i2);
        int i3 = Bytes.toInt(bytes);
        Assert.assertEquals(i, i3);
    }

    private void test(long most, long least) {
        byte[] bytes1 = Bytes.toBytes(most);
        byte[] bytes2 = Bytes.toBytes(least);
        byte[] add = Bytes.add(bytes1, bytes2);
        byte[] bytes = BytesUtils.longLongToBytes(most, least);
        Assert.assertArrayEquals(add, bytes);


        long[] longLong = BytesUtils.bytesToLongLong(bytes);
        Assert.assertEquals(most, longLong[0]);
        Assert.assertEquals(least, longLong[1]);


        long bMost = BytesUtils.bytesToLong(bytes, 0);
        long bLeast = BytesUtils.bytesToLong(bytes, 8);
        Assert.assertEquals(most, bMost);
        Assert.assertEquals(least, bLeast);

        byte bBytes[] = new byte[16];
        BytesUtils.writeLong(most, bBytes, 0);
        BytesUtils.writeLong(least, bBytes, 8);
        Assert.assertArrayEquals(add, bBytes);
    }

    @Test
    public void testAddStringLong() throws Exception {
        byte[] testAgents = BytesUtils.add("testAgent", 11L);
        byte[] buf = Bytes.add(Bytes.toBytes("testAgent"), Bytes.toBytes(11L));
        Assert.assertArrayEquals(testAgents, buf);
    }
    
	@Test
	public void testMerge() {
		byte[] b1 = new byte[] { 1, 2 };
		byte[] b2 = new byte[] { 3, 4 };

		byte[] b3 = BytesUtils.merge(b1, b2);

		Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4 }, b3));
	}
}
