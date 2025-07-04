package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.io.IOException;
import java.util.Arrays;

/**
 * Copy from sematext/HBaseWD
 * Defines the way row keys are distributed
 *
 * @author Alex Baranau
 */
public interface RowKeyDistributor {
    byte[] getDistributedKey(byte[] originalKey);

    byte[] getOriginalKey(byte[] adjustedKey);

    byte[][] getAllDistributedKeys(byte[] originalKey);

    /**
     * Gets all distributed intervals based on the original start & stop keys.
     * Used when scanning all buckets based on start/stop row keys. Should return keys so that all buckets in which
     * records between originalStartKey and originalStopKey were distributed are "covered".
     * @param originalStartKey start key
     * @param originalStopKey stop key
     * @return array[Pair(startKey, stopKey)]
     */
    default Pair<byte[], byte[]>[] getDistributedIntervals(byte[] originalStartKey, byte[] originalStopKey) {
        byte[][] startKeys = getAllDistributedKeys(originalStartKey);
        byte[][] stopKeys;
        if (Arrays.equals(originalStopKey, HConstants.EMPTY_END_ROW)) {
            Arrays.sort(startKeys, Bytes.BYTES_RAWCOMPARATOR);
            stopKeys = stopKey(startKeys);
        } else {
            stopKeys = getAllDistributedKeys(originalStopKey);
            assert stopKeys.length == startKeys.length;
        }

        @SuppressWarnings("unchecked")
        Pair<byte[], byte[]>[] intervals = new Pair[startKeys.length];
        for (int i = 0; i < startKeys.length; i++) {
            intervals[i] = new Pair<>(startKeys[i], stopKeys[i]);
        }

        return intervals;
    }

    private byte[][] stopKey(byte[][] startKeys) {
        byte[][] stopKeys = new byte[startKeys.length][];
        if (stopKeys.length - 1 >= 0) {
            System.arraycopy(startKeys, 1, stopKeys, 0, stopKeys.length - 1);
        }
        stopKeys[stopKeys.length - 1] = HConstants.EMPTY_END_ROW;
        return stopKeys;
    }

    default Scan[] getDistributedScans(Scan original) throws IOException {
        Pair<byte[], byte[]>[] intervals = getDistributedIntervals(original.getStartRow(), original.getStopRow());

        Scan[] scans = new Scan[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            scans[i] = new Scan(original);
            scans[i].setStartRow(intervals[i].getFirst());
            scans[i].setStopRow(intervals[i].getSecond());
        }
        return scans;
    }

}
