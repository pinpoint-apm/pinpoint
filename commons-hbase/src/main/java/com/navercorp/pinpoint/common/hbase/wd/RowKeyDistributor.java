/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Arrays;

/**
 * Copy from sematext/HBaseWD
 * Defines the way row keys are distributed
 *
 * @author Alex Baranau
 */
public interface RowKeyDistributor {
    ByteHasher getByteHasher();

    int getSaltKeySize();
    
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

    default DistributedScan getDistributedScans(Scan original) throws IOException {
        Pair<byte[], byte[]>[] intervals = getDistributedIntervals(original.getStartRow(), original.getStopRow());

        Scan[] scans = new Scan[intervals.length];
        for (int i = 0; i < intervals.length; i++) {
            Pair<byte[], byte[]> interval = intervals[i];
            byte[] start = interval.getFirst();
            byte[] stop = interval.getSecond();
            scans[i] = copyScan(original, start, stop, i);
        }
        return new DistributedScan(scans, this.getSaltKeySize());
    }

    private @NonNull Scan copyScan(Scan original, byte[] start, byte[] stop, int i) throws IOException {
        Scan copy = new Scan(original);

        copy.setStartRow(start);
        copy.setStopRow(stop);

        final String scanId = original.getId();
        if (scanId != null) {
            copy.setId(scanId + "-" + i);
        }
        return copy;
    }

}
