package com.profiler.common.util;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * 
 * @author netspider
 * 
 */
public class TerminalSpanUtils {

	// TODO global variable로 변경
	private static final int APPLICATION_NAME_MAX_LEN = 24;

	/**
	 * columnName format = "SERVICETYPE(4byte)" + "APPLICATIONNAME"
	 * 
	 * @param serviceType
	 * @param applicationName
	 * @return
	 */
	public static byte[] makeColumnName(short serviceType, String applicationName) {
		byte[] t = Bytes.toBytes(serviceType);
		byte[] n = Bytes.toBytes(applicationName);

		byte[] buf = new byte[t.length + n.length];
		System.arraycopy(t, 0, buf, 0, t.length);
		System.arraycopy(n, 0, buf, t.length, n.length);

		return buf;
	}

	public static short getServiceTypeFromColumnName(byte[] bytes) {
		return (short) (((bytes[0] & 0xff) << 8) | ((bytes[1] & 0xff)));
	}

	public static String getApplicationNameFromColumnName(byte[] bytes) {
		byte[] temp = new byte[bytes.length - 2];
		System.arraycopy(bytes, 2, temp, 0, bytes.length - 2);
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

		byte[] buf = new byte[APPLICATION_NAME_MAX_LEN + slot.length];
		System.arraycopy(n, 0, buf, 0, n.length);
		System.arraycopy(slot, 0, buf, APPLICATION_NAME_MAX_LEN, slot.length);

		return buf;
	}

	public static String getApplicationNameFromRowKey(byte[] bytes) {
		byte[] temp = new byte[bytes.length - 8];
		System.arraycopy(bytes, 0, temp, 0, bytes.length - 8);
		return new String(temp).trim();
	}
}
