package com.profiler.common.util;

import junit.framework.Assert;

import org.junit.Test;

import com.profiler.common.ServiceType;

public class TerminalSpanUtilsTest {

	@Test
	public void column() {
		short t = ServiceType.ARCUS.getCode();
		String a = "Hello, World.";

		short[] slots = ServiceType.ARCUS.getHistogramSlots();

		for (int i = 0; i < slots.length; i++) {
			short slot = slots[i];

			byte[] columnName = TerminalSpanUtils.makeColumnName(t, a, slot - 1);

			Assert.assertEquals(t, TerminalSpanUtils.getDestServiceTypeFromColumnName(columnName));
			Assert.assertEquals(slot, TerminalSpanUtils.getHistogramSlotFromColumnName(columnName));
			Assert.assertEquals(a, TerminalSpanUtils.getDestApplicationNameFromColumnName(columnName));
		}

		// check max value
		byte[] columnName = TerminalSpanUtils.makeColumnName(t, a, 100000);

		Assert.assertEquals(t, TerminalSpanUtils.getDestServiceTypeFromColumnName(columnName));
		Assert.assertEquals(slots[slots.length - 1], TerminalSpanUtils.getHistogramSlotFromColumnName(columnName));
		Assert.assertEquals(a, TerminalSpanUtils.getDestApplicationNameFromColumnName(columnName));
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
