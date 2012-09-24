package com.profiler.common.util;

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

	private void test(long most, long least) {
		byte[] bytes1 = Bytes.toBytes(most);
		byte[] bytes2 = Bytes.toBytes(least);
		byte[] add = Bytes.add(bytes1, bytes2);

		byte[] bytes = BytesUtils.longLongToBytes(most, least);

		long[] org = BytesUtils.bytesToLongLong(bytes);

		Assert.assertEquals(most, org[0]);
		Assert.assertEquals(least, org[1]);

		Assert.assertArrayEquals(add, bytes);
	}
}
