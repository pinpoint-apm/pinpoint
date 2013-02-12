package com.profiler.common.util;

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
		byte[] t = Bytes.toBytes(serviceType);
		byte[] s = findResponseHistogramSlotNo(serviceType, elapsed);
		byte[] n = Bytes.toBytes(applicationName);

		byte[] buf = new byte[t.length + s.length + n.length];
		System.arraycopy(t, 0, buf, 0, t.length);
		System.arraycopy(s, 0, buf, t.length, s.length);
		System.arraycopy(n, 0, buf, t.length + s.length, n.length);

		return buf;
	}

	private static byte[] findResponseHistogramSlotNo(short serviceType, int elapsed) {
		short[] slots = ServiceType.findServiceType(serviceType).getHistogramSlots();

		for (short slot : slots) {
			if (elapsed < slot) {
				return Bytes.toBytes(slot);
			}
		}

		return new byte[] { 0, 0 };
	}

	public static short getDestServiceTypeFromColumnName(byte[] bytes) {
		return (short) (((bytes[0] & 0xff) << 8) | ((bytes[1] & 0xff)));
	}

	public static short getHistogramSlotFromColumnName(byte[] bytes) {
		return (short) (((bytes[2] & 0xff) << 8) | ((bytes[3] & 0xff)));
	}

	public static String getDestApplicationNameFromColumnName(byte[] bytes) {
		byte[] temp = new byte[bytes.length - 4]; // 4 = servietype + responsecode
		System.arraycopy(bytes, 4, temp, 0, bytes.length - 4);
		return new String(temp);
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
