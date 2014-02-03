package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * <pre>
 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPNAMELEN(2bytes) + APPLICATIONNAME(str) + HOST(str)
 * </pre>
 *
 * @author netspider
 * @author emeroad
 */
public class ApplicationMapStatisticsUtils {

    public static byte[] makeColumnName(short serviceType, String applicationName, String destHost, short slotNumber) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (destHost == null) {
            // throw new NullPointerException("destHost must not be null");
            destHost = "";
        }
        byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
        byte[] slotNumberBytes = Bytes.toBytes(slotNumber);
        byte[] applicationNameBytes = Bytes.toBytes(applicationName);
        byte[] applicationNameLenBytes = Bytes.toBytes((short) applicationNameBytes.length);
        byte[] destHostBytes = Bytes.toBytes(destHost);

        return BytesUtils.concat(serviceTypeBytes, slotNumberBytes, applicationNameLenBytes, applicationNameBytes, destHostBytes);
    }

    public static short getSlotNumber(short serviceType, int elapsed, boolean isError) {
        if (isError) {
            return HBaseTables.STATISTICS_CQ_ERROR_SLOT_NUMBER;
        } else {
            return findResponseHistogramSlotNo(serviceType, elapsed);
        }
    }

    public static byte[] makeColumnName(short serviceType, String agentId, int elapsed, boolean isError) {
        short slotNumber = getSlotNumber(serviceType, elapsed, isError);
        return makeColumnName(agentId, slotNumber);
    }

    public static byte[] makeColumnName(String agentId, short columnSlotNumber) {
        if (agentId == null) {
            agentId = "";
        }
        final byte[] slotNumber = Bytes.toBytes(columnSlotNumber);
        final byte[] agentIdBytes = Bytes.toBytes(agentId);

        return BytesUtils.concat(slotNumber, agentIdBytes);
    }


    private static short findResponseHistogramSlotNo(short serviceType, int elapsed) {
        final HistogramSchema histogramSchema = ServiceType.findServiceType(serviceType).getHistogramSchema();
        final HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsed);
        return histogramSlot.getSlotTime();
    }

    public static short getDestServiceTypeFromColumnName(byte[] bytes) {
        return BytesUtils.bytesToShort(bytes, 0);
    }

    /**
     * @param bytes
     * @return <pre>
     *         0 > : ms
     *         0 : slow
     *         -1 : error
     *         </pre>
     */
    public static short getHistogramSlotFromColumnName(byte[] bytes) {
        return BytesUtils.bytesToShort(bytes, 2);
    }

    public static String getDestApplicationNameFromColumnName(byte[] bytes) {
        final short length = BytesUtils.bytesToShort(bytes, 4);
        return BytesUtils.toStringAndRightTrim(bytes, 6, length);
    }

    public static String getHost(byte[] bytes) {
        int offset = 6 + BytesUtils.bytesToShort(bytes, 4);

        if (offset == bytes.length) {
            return null;
        }
        return BytesUtils.toStringAndRightTrim(bytes, offset, bytes.length - offset);
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
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
        byte[] temp = new byte[applicationNameLength];
        System.arraycopy(bytes, 2, temp, 0, applicationNameLength);
        return BytesUtils.toString(temp); //.trim();
    }

    public static short getApplicationTypeFromRowKey(byte[] bytes) {
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
        return BytesUtils.bytesToShort(bytes, applicationNameLength + 2);
    }

    public static long getTimestampFromRowKey(byte[] bytes) {
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
        return TimeUtils.recoveryCurrentTimeMillis(BytesUtils.bytesToLong(bytes, applicationNameLength + 4));
    }
}
