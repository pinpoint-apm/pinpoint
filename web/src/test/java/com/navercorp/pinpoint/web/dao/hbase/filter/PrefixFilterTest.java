package com.nhn.pinpoint.web.dao.hbase.filter;

import java.util.Arrays;

import org.junit.Test;

import com.nhn.pinpoint.common.util.BytesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixFilterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Test
	public void prefixInt() {

		byte[] before = new byte[4];

		for (int i = 1000; i < 1100; i++) {
			byte[] buffer = new byte[4];
			BytesUtils.writeVar32(i, buffer, 0);

			logger.debug(compare(before, buffer) + ", " + compare(buffer, before) + ", " + compare(buffer, buffer));

			before = Arrays.copyOf(buffer, 4);

            logger.debug(Arrays.toString(buffer));
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
