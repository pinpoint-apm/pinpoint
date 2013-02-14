package com.profiler.common.util;

import com.profiler.common.Histogram;
import com.profiler.common.HistogramSlot;
import junit.framework.Assert;

import org.junit.Test;

import com.profiler.common.ServiceType;

import java.util.List;

public class TerminalSpanUtilsTest {

	@Test
	public void column() {
		short arcusCode = ServiceType.ARCUS.getCode();
		String name = "Hello, World.";

		Histogram arcusHistogram = ServiceType.ARCUS.getHistogram();
        List<HistogramSlot> histogramSlotList = arcusHistogram.getHistogramSlotList();
        for (int i = 0; i < histogramSlotList.size(); i++) {
			HistogramSlot slot = histogramSlotList.get(i);
            short slotTime = (short) slot.getSlotTime();

            byte[] columnName = TerminalSpanUtils.makeColumnName(arcusCode, name, slotTime - 1);

			Assert.assertEquals(arcusCode, TerminalSpanUtils.getDestServiceTypeFromColumnName(columnName));
			Assert.assertEquals(slot.getSlotTime(), TerminalSpanUtils.getHistogramSlotFromColumnName(columnName));
			Assert.assertEquals(name, TerminalSpanUtils.getDestApplicationNameFromColumnName(columnName));
		}

		// check max value
		byte[] columnName = TerminalSpanUtils.makeColumnName(arcusCode, name, 100000);

		Assert.assertEquals(arcusCode, TerminalSpanUtils.getDestServiceTypeFromColumnName(columnName));
		Assert.assertEquals(0, TerminalSpanUtils.getHistogramSlotFromColumnName(columnName));
		Assert.assertEquals(name, TerminalSpanUtils.getDestApplicationNameFromColumnName(columnName));
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
