package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.HistogramSchema;
import org.apache.hadoop.hbase.util.Bytes;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HBaseTables;

/**
 * <pre>
 * columnName format = SLOT(2bytes) + agentId(str)
 * </pre>
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationStatisticsUtils {
	
	public static byte[] makeColumnName(String agentId, short slotNumber) {
		if (agentId == null) {
			agentId = "";
		}
		byte[] slotNumberBytes = Bytes.toBytes(slotNumber);
        byte[] agentIdBytes = Bytes.toBytes(agentId);
		
		return BytesUtils.concat(slotNumberBytes, agentIdBytes);
	}



    public static short getSlotNumber(short serviceType, int elapsed, boolean isError) {
        if (isError) {
            return HBaseTables.STATISTICS_CQ_ERROR_SLOT_NUMBER;
        } else {
            return findResponseHistogramSlotNo(serviceType, elapsed);
        }
    }

    private static short findResponseHistogramSlotNo(short serviceType, int elapsed) {
		final HistogramSchema histogramSchema = ServiceType.findServiceType(serviceType).getHistogramSchema();
		final HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsed);
		return histogramSlot.getSlotTime();
	}

	/**
	 * 
	 * @param bytes
	 * @return <pre>
	 * 0 > : ms
	 * 0 : slow
	 * -1 : error
	 * </pre>
	 */
	public static short getHistogramSlotFromColumnName(byte[] bytes) {
		return BytesUtils.bytesToShort(bytes, 0);
	}

	/**
	 * <pre>
	 * rowkey format = "APPLICATIONNAME(max 24bytes)" + apptype(2byte) + "TIMESTAMP(8byte)"
	 * </pre>
	 * 
	 * @param applicationName
	 * @param timestamp
	 * @return
	 */
	public static byte[] makeRowKey(String applicationName, short applicationType, long timestamp) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName must not be null");
		}

		byte[] applicationnameBytes = Bytes.toBytes(applicationName);
		byte[] applicationnameBytesLength = Bytes.toBytes((short) applicationnameBytes.length);
		// byte[] offset = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationnameBytes.length];
		byte[] applicationtypeBytes = Bytes.toBytes(applicationType);
		byte[] slot = Bytes.toBytes(TimeUtils.reverseCurrentTimeMillis(timestamp));

		return BytesUtils.concat(applicationnameBytesLength, applicationnameBytes, applicationtypeBytes, slot);
	}

	public static String getApplicationNameFromRowKey(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
		return BytesUtils.toString(bytes, 2, applicationNameLength); //.trim();
	}

	public static short getApplicationTypeFromRowKey(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
		return BytesUtils.bytesToShort(bytes, applicationNameLength + 2);
	}
	
	public static long getTimestampFromRowKey(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
		return TimeUtils.recoveryCurrentTimeMillis(BytesUtils.bytesToLong(bytes, applicationNameLength + 4));
	}
}
