package com.nhn.pinpoint.web.dao.hbase.filter;

import java.util.Arrays;

import org.junit.Test;

import com.nhn.pinpoint.common.util.BytesUtils;

public class PrefixFilterTest {

	@Test
	public void prefixInt() {

		byte[] before = new byte[4];

		for (int i = 1000; i < 1100; i++) {
			byte[] buffer = new byte[4];
			BytesUtils.writeVar32(i, buffer, 0);

			System.out.println(compare(before, buffer) + ", " + compare(buffer, before) + ", " + compare(buffer, buffer));

			before = Arrays.copyOf(buffer, 4);

			System.out.println(Arrays.toString(buffer));
		}
	}

	public int compare(byte[] left, byte[] right) {
		for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
			int a = (left[i] & 0xff);
			int b = (right[j] & 0xff);
			if (a != b) {
				return a - b;
			}
		}
		return left.length - right.length;
	}

}
