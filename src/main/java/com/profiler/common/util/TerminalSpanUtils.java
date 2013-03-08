package com.profiler.common.util;

import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.Histogram;
import com.profiler.common.HistogramSlot;
import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HBaseTables;

/**
 * <pre>
 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPNAMELEN(2bytes) + APPLICATIONNAME(str) + HOST(str)
 * </pre>
 * 
 * @author netspider
 * 
 */
public class TerminalSpanUtils {

	public static byte[] makeColumnName(short serviceType, String applicationName, String destHost, int elapsed, boolean isError) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (destHost == null) {
            throw new NullPointerException("destHost must not be null");
        }
		byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
		byte[] slotNumber;
		if (isError) {
			slotNumber = HBaseTables.TERMINAL_STATISTICS_CQ_ERROR_SLOT;
		} else {
			slotNumber = findResponseHistogramSlotNo(serviceType, elapsed);
		}
		byte[] applicationNameBytes = Bytes.toBytes(applicationName);
		byte[] applicationNameLenBytes = Bytes.toBytes((short) applicationNameBytes.length);
		byte[] destHostBytes = Bytes.toBytes(destHost);

		return BytesUtils.concat(serviceTypeBytes, slotNumber, applicationNameLenBytes, applicationNameBytes, destHostBytes);
	}

	private static byte[] findResponseHistogramSlotNo(short serviceType, int elapsed) {
		Histogram histogram = ServiceType.findServiceType(serviceType).getHistogram();
		HistogramSlot histogramSlot = histogram.findHistogramSlot(elapsed);
		short slotTime = (short) histogramSlot.getSlotTime();
		return Bytes.toBytes(slotTime);
	}

	public static short getDestServiceTypeFromColumnName(byte[] bytes) {
		return BytesUtils.bytesToShort(bytes, 0);
	}

	public static short getHistogramSlotFromColumnName(byte[] bytes) {
		return BytesUtils.bytesToShort(bytes, 2);
	}

	public static String getDestApplicationNameFromColumnName(byte[] bytes) {
		return new String(bytes, 6, BytesUtils.bytesToShort(bytes, 4));
	}

	public static String getHost(byte[] bytes) {
		int offset = 6 + BytesUtils.bytesToShort(bytes, 4);
		
		if (offset == bytes.length) {
			return null;
		}

		return new String(bytes, offset, bytes.length - offset);
	}

	/**
	 * rowkey format = "APPLICATIONNAME(max 24bytes)" + "TIMESTAMP(8byte)"
	 * 
	 * @param applicationName
	 * @param time
	 * @return
	 */
	public static byte[] makeRowKey(String applicationName, long time) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
		byte[] slot = Bytes.toBytes(time);
		byte[] applicationnameBytes = Bytes.toBytes(applicationName);

		byte[] buf = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN + 8];
		System.arraycopy(applicationnameBytes, 0, buf, 0, applicationnameBytes.length);
		System.arraycopy(slot, 0, buf, HBaseTables.APPLICATION_NAME_MAX_LEN, 8);

		return buf;
	}

	public static String getApplicationNameFromRowKey(byte[] bytes) {
		byte[] temp = new byte[bytes.length - 8];
		System.arraycopy(bytes, 0, temp, 0, bytes.length - 8);
		return new String(temp).trim();
	}
}
