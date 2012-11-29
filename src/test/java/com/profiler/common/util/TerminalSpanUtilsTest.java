package com.profiler.common.util;

import junit.framework.Assert;

import org.junit.Test;

public class TerminalSpanUtilsTest {

	@Test
	public void column() {
		short t = (short) 3001;
		String a = "Hello, World.";

		byte[] columnName = TerminalSpanUtils.makeColumnName(t, a);

		Assert.assertEquals(t, TerminalSpanUtils.getServiceTypeFromColumnName(columnName));
		Assert.assertEquals(a, TerminalSpanUtils.getApplicationNameFromColumnName(columnName));
	}

	@Test
	public void rowkey() {
		String keystr = "ROWKEY";
		long time = System.currentTimeMillis();

		byte[] rowkey = TerminalSpanUtils.makeRowKey(keystr, time);

		String k = TerminalSpanUtils.getApplicationNameFromRowKey(rowkey);

		System.out.println(keystr.getBytes().length);
		System.out.println(k.getBytes().length);

	}
}
