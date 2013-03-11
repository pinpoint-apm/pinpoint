package com.profiler.common.util;

import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.Histogram;
import com.profiler.common.HistogramSlot;
import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HBaseTables;

/**
 * <pre>
 * rowkey format = dest application name(str) + servicetype(2bytes) + timeslot(8bytes)
 * 
 * columnName format = histogram SLOT(2bytes)
 * </pre>
 * 
 * @author netspider
 * 
 */
public class ClientStatUtils {

	public static byte[] makeColumnName(int elapsed, boolean isError) {
		byte[] slotNumber;
		if (isError) {
			slotNumber = HBaseTables.CLIENT_STATISTICS_CQ_ERROR_SLOT;
		} else {
			slotNumber = findResponseHistogramSlotNo(elapsed);
		}

		return slotNumber;
	}

	private static byte[] findResponseHistogramSlotNo(int elapsed) {
		Histogram histogram = ServiceType.CLIENT.getHistogram();
		HistogramSlot histogramSlot = histogram.findHistogramSlot(elapsed);
		short slotTime = (short) histogramSlot.getSlotTime();

		return Bytes.toBytes(slotTime);
	}

	public static byte[] makeRowKey(String destApplicationName, short destServiceType, long rowTimeSlot) {
		if (destApplicationName == null) {
			throw new NullPointerException("applicationName must not be null");
		}
		byte[] rowTimeSlotBytes = Bytes.toBytes(rowTimeSlot);
		byte[] destApplicationnameBytes = Bytes.toBytes(destApplicationName);
		byte[] destServiceTypeBytes = Bytes.toBytes(destServiceType);
		return BytesUtils.concat(destApplicationnameBytes, destServiceTypeBytes, rowTimeSlotBytes);
	}

	public static String getApplicationNameFromRowKey(byte[] rowKey) {
		// rowkey = applicationname(?) + servicetype(2) + timestamp(8)
		return new String(rowKey, 0, rowKey.length - 8 - 2);
	}

	public static short getApplicationServiceTypeFromRowKey(byte[] rowKey) {
		return Bytes.toShort(rowKey, rowKey.length - 8 - 2, 2);
	}

	public static short getHistogramSlotFromColumnName(byte[] bytes) {
		return BytesUtils.bytesToShort(bytes, 0);
	}
}
