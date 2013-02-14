package com.profiler.common.util;

import com.profiler.common.Histogram;
import com.profiler.common.HistogramSlot;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HBaseTables;

/**
 * 
 * @author netspider
 * 
 */
public class TerminalSpanUtils {

	/**
	 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPLICATIONNAME(str)
	 * 
	 * @param serviceType
	 * @param applicationName
	 * @param elapsed
	 * @return
	 */
	public static byte[] makeColumnName(short serviceType, String applicationName, int elapsed) {
		byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
		byte[] slotNumber = findResponseHistogramSlotNo(serviceType, elapsed);
		byte[] applicationNameBytes = Bytes.toBytes(applicationName);

		byte[] buf = new byte[serviceTypeBytes.length + slotNumber.length + applicationNameBytes.length];
		System.arraycopy(serviceTypeBytes, 0, buf, 0, serviceTypeBytes.length);
		System.arraycopy(slotNumber, 0, buf, serviceTypeBytes.length, slotNumber.length);
		System.arraycopy(applicationNameBytes, 0, buf, serviceTypeBytes.length + slotNumber.length, applicationNameBytes.length);

		return buf;
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
		return new String(bytes, 4, bytes.length - 4);
	}

	/**
	 * rowkey format = "APPLICATIONNAME(max 24bytes)" + "TIMESTAMP(8byte)"
	 * 
	 * @param applicationName
	 * @param time
	 * @return
	 */
	public static byte[] makeRowKey(String applicationName, long time) {
		byte[] slot = Bytes.toBytes(time);
		byte[] n = Bytes.toBytes(applicationName);

		byte[] buf = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN + slot.length];
		System.arraycopy(n, 0, buf, 0, n.length);
		System.arraycopy(slot, 0, buf, HBaseTables.APPLICATION_NAME_MAX_LEN, slot.length);

		return buf;
	}

	public static String getApplicationNameFromRowKey(byte[] bytes) {
		byte[] temp = new byte[bytes.length - 8];
		System.arraycopy(bytes, 0, temp, 0, bytes.length - 8);
		return new String(temp).trim();
	}
}
