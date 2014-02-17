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
		return ApplicationMapStatisticsUtils.makeRowKey(applicationName, applicationType, timestamp);
	}

	public static String getApplicationNameFromRowKey(byte[] bytes) {
        return ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(bytes);
	}

	public static short getApplicationTypeFromRowKey(byte[] bytes) {
        return ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(bytes);
	}
	
	public static long getTimestampFromRowKey(byte[] bytes) {
        return ApplicationMapStatisticsUtils.getTimestampFromRowKey(bytes);
	}
}
